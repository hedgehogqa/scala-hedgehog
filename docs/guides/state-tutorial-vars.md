---
title: 'State-Based Property Testing Tutorial (Part 2 - Vars)'
sidebar_label: 'State-Based Testing (2)'
slug: '/guides-state-tutorial-vars'
---
## State-Based Property Testing Tutorial (Part 2 - Vars)

This tutorial continues on from the [first tutorial](state-tutorial.md).

## Follow Along

Please feel to play along and tinker with the code.

```
$ ./sbt
> example/runMain hedgehog.examples.state.CRTest
```

## The Problem

To start with let's define an interface/abstraction for something we want to
test against, part of a basic create/read/update/delete or [CRUD] store, which
I'm calling `CR` as we only support create/read to simplify the example:

[CRUD]: https://en.wikipedia.org/wiki/Create,_read,_update_and_delete

```scala
trait CR {

  def create(v: String): CRId

  def read(id: CRId): Option[String]
}

case class CRId(render: String)
```

NOTE: The big difference here from the previous `KV` is that the service is
returning it's own identifier instead of being passed on. That has implications
for testing the state with Hedgehog, which require something extra...

### Model (take 1)

Similar to our previous example, this also seems fairly straight forward.
But we're about to hit a snag...

```scala
case class State(map: Map[CRId, String])
```

And our command input types:

```scala
case class Create(value: String)
case class Read(id: CRId)
```

Now let's step throught the `Command` functions for `create`/`read` like
before:

### Gen

Generation is fairly typical. Note that we can't really usefully create a
`Read` value until we have at least one value in `State`.

```scala
def gen(s: State): Option[Gen[Create]] =
  Some(for {
    v <- Gen.string(Gen.ascii, Range.linear(1, 10))
  } yield Create(v))

def gen(s: State): Option[Gen[Read]] =
  s.map.keys.toList match {
    case Nil =>
      // We haven't created anything yet
      None
    case h :: t =>
      Some(Gen.element(h, t).map(Read(_)))
  }
```

### Execute (take 1)

So far everything lines up just like before:

```scala
def execute(i: Create): Either[String, CRId] =
  Right(cr.create(i.key, i.value))

def execute(i: Read): Either[String, Option[String]] =
  Right(cr.read(i.key))
```

### Update

```scala
def update(s: State, i: Create, o: Var[CRId]): State =
  s.copy(map = s.map + (o -> i.value))

// There are no side-effects for get, so nothing to do
def update(s: State, i: Read): State =
  s
```

Hang on, what's that `Var[CRid]` doing?!? Why isn't it just a concrete `CRId`?
That code doesn't even compile!

### WTF

It turns out that Hedgehog forces a very clear separation for functions that
can access the "real world" state, and those that can't. The reasons for this
are a little involved, but the basic idea is that we can generate a full
sequence of commands up-front, without having called `execute`. So the
question/problem then becomes, how do we capture the "result" to be used in
`gen`, like we _need_ to do for `read`?

This is where `Var` and `Environment` come in. For each `Command.execute`,
Hedgehog allocates a unique `Name` (just a plain old incrementing `Int`), and
passed _that_ to the "pure" functions, like `update` and `gen`. It's only when
we need the _concrete_ values do we get access to the `Environment` to look
them up.

Here is the relevant data types, that should hopefully make more sense now?

```scala
case class Name(value: Int)

case class Environment(map: Map[Name, Any])

case class Var[A](name: Name) {
  def get(env: Env): A =
    env.map(name)
}
```

### Model (take 2)

So we will need to tweak our model and input slightly to use `Var`:

```scala
case class State(map: Map[Var[CRId], String])

case class Read(id: Var[CRId])
```

### Execute (take 2)

So let's try that again. Note that we now use `Environment` in the `Read`
version to `get` the concrete `CRId` returned from `create`.

```scala
def execute(env: Environment, i: Create): Either[String, CRId] =
  Right(cr.create(i.key, i.value))

def execute(env: Environment, i: Read): Either[String, Option[String]] =
  Right(cr.read(i.key.get(env)))
```

### Ensure

This is identical to `KV`, although notice we _could_ use `Environment` here if
we needed to, unlike `update`.

```scala
// Almost the reverse of update, for side-effect operations we may not observe anything just yet
def ensure(env: Environment, s0: State, s1: State, i: Create, o: CRId): Result =
  Result.success

def ensure(env: Environment, s0: State, s1: State, i: Read, o: Option[String]): Result =
  s1.map.get(i.key) ==== o
```

## Debugging

Let's look at the output of a failed test again:

```scala
Var(Name(0)) = Create(a)
Var(Name(1)) = Create(b)
Var(Name(2)) = Read(Var(Name(0)))
```

We can start to see that each command is shown to be assigning the result to a
new, unique `Var`. We aren't looking at the _actual_ `CRId` values.  Hedgehog
(currently) can't tell that `Read` doesn't actually return anything (useful)
and so the `Unit` value is also asigned to a new `Var`.

## Resources

Unfortunately there isn't much documentation for this approach at the moment.

- [Finding Race Conditions in Erlang with QuickCheck and PULSE](http://www.cse.chalmers.se/~nicsma/papers/finding-race-conditions.pdf)
  Hedgehog follows a vague description of this implementation which is mentioned
  in this paper.
- [State machine testing with Hedgehog](https://teh.id.au/posts/2017/07/15/state-machine-testing/index.html#parameterised-actions)
- [Haskell Differences](haskell-differences.md#state-vars)
