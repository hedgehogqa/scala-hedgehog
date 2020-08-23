---
title: 'State-Based Property Testing Tutorial'
sidebar_label: 'State-Based Testing (1)'
slug: '/guides-state-tutorial'
---

## State-Based Property Testing Tutorial

## Follow Along

Please feel to play along and tinker with the code.

```
$ ./sbt
> example/runMain hedgehog.examples.state.KVTest
```

## The Problem

To start with let's define an interface/abstraction for something we want to
test against, the most basic key/value store:

```scala
trait KV {

  def put(k: String, v: String): Unit

  def get(k: String): Option[String]
}
```

Now let's write some normal property tests to test it:

```scala
def testPut(kv: KV): Property =
  for {
    k <- genKey
    v <- genValue
  } yield {
    kv.put(k, v)
    kv.get(k) ==== Some(v)
  }

def testGetMissing(kv: KV): Property =
  for {
    v <- genValue
  } yield {
    kv.get(k) ==== None
  }
```

So far so good. But do we think we have tested all the cases? Think about that
for a second and let's see what happens...

We're looking at an interface with just two functions, but there are infinite
ways in which we can interact with them. How well do are tests scale when we add
_one_ more function (ie delete)? Or ten?

What we really want to do is let our property testing library generate the
interactions themselves!

## Model

At a conceptual level the idea with state-based testing is to "model" the real
world with something pure (ie in memory). We then execute real commands against
our system/api, and compare the result with what our model expects.

![](https://res.infoq.com/presentations/testing-techniques-case-study/en/slides/sl6.jpg)

At the technical level the current approach to writing a state-based test is to
implement a series of very specific functions for each operation or "command"
you want to test.

Firstly let's create our _pure_ domain model for testing. In this case it's
trivial to think of a key-value store as an immutable `Map`.

```scala
case class State(map: Map[String, String])
```

For each command you quite often end up just reifying the function arguments in
to a data type:

```scala
case class Put(key: String, value: String)
case class Get(key: String)
```

For each command you need to implement the [Command] interface. This can look
quite daunting so let's step through it:

[Command]: https://github.com/hedgehogqa/scala-hedgehog/blob/master/core/shared/src/main/scala/hedgehog/state/Command.scala

```scala
def command(kv: KV): CommandIO[State] =

  new Command[State, Input, Output] {

    def gen(s: State): Option[Gen[Input]]

    def execute(i: Input): Either[String, Output]

    def update(s: State, i: Input): State

    def ensure(s0: State, s1: State, i: Input, o: Output): Result
  }
```

### Gen

Firstly, we need to be able to generate random inputs to our command.

The key thing to note is that we take in the current state, which we might want
to use to generate some values.  There might be cases where no valid input can
be generated. For example you need to call put before you can call get.  One
simple way to generate our operations would be something like:

```scala
def gen(s: State): Option[Gen[Put]] =
  Some(for {
    k <- Gen.string(Gen.ascii, Range.linear(1, 10))
    v <- Gen.string(Gen.ascii, Range.linear(1, 10))
  } yield Put(k, v))

def gen(s: State): Option[Gen[Get]] =
  Some(Gen.string(Gen.ascii, Range.linear(1, 10))
    .map(Get(_)))
```

But how often do we generate the same keys in either case? Shouldn't we try to
generate similar keys sometimes?

A slightly better attempt:

```scala
def genKey: Gen[String] =
  Gen.string(Gen.ascii, Range.linear(1, 10))

def gen(s: State): Option[Gen[Put]] =
  Some(for {
    k <- s.map.keys.toList match {
      case Nil =>
        genKey
      case h :: t =>
        // Choice between known and unknown keys
        Gen.frequency1(
          50 -> genKey
        , 50 -> Gen.element(h, t)
        )
    }
    // Always generate a unique value
    v <- Gen.string(Gen.ascii, Range.linear(1, 10))
  } yield Put(k, v))

def gen(s: State): Option[Gen[Get]] =
  Some((s.map.keys.toList match {
    case Nil =>
      genKey
    case h :: t =>
      // Mostly get values we know to exist, but also try values that don't
      Gen.frequency1(
        80 -> Gen.element(h, t)
      , 20 -> genKey
      )
  }).map(Get(_)))
```

That's definitely a little more involved, but you can see we're getting a good
spread of put/get values with known/duplicate and unknown keys. That should
cover everything, which is a critical component of _good_ property testing in
general.

### Execute

This is the easy bit, just call our operation. Note the lack of our model/state,
you can also interact with the "real world".

```scala
def execute(i: Put): Either[String, Unit] =
  Right(kv.put(i.key, i.value))

def execute(i: Get): Either[String, Option[String]] =
  Right(kv.get(i.key))
```

### Update

This is where we need to "model" what we expect that operation to do in our pure state.

```scala
def update(s: State, i: Put): State =
  s.copy(map = s.map + (i.key -> i.value))

// There are no side-effects for get, so nothing to do
def update(s: State, i: Get): State =
  s
```

### Ensure

This is the "post condition". What did we expect to happen in the real world vs
our model?

Just as a side note, the `s0` means the state before update is run, and `s1` is
the current state. It's always possible, and fairly common, to just use `s1` and
the input value.

```scala
// Almost the reverse of update, for side-effect operations we may not observe anything just yet
def ensure(s0: State, s1: State, i: Put, o: Unit): Result =
  Result.success

def ensure(s0: State, s1: State, i: Get, o: Option[String]): Result =
  s1.map.get(i.key) ==== o
```

## FAQ

The common question here is "won't we just end up with two implementations"? Yes
and no. Obviously this is a trivial example. The model is so simple that we can
actually have a 100% full re-implementation. This won't always be the case, we
may only want to guarantee some invariants are true, but not the exact
behaviour. Also remember that we haven't actually looked at the implementation
of KV yet. It might be storing keys on files, or across the network with
multiple copies. If there are any bugs in any of that code it's quite likely we
will find it at some point.

## Sequential

So the final piece is to hook up all our commands to form a property. It would look something like this:

```scala
def testKVFileSequential: Property = {
  val root = File("tmpdir")
  // Create a simple file-system KV store
  val kv = KV.file(root)
  sequential(
    // The range of commands to generate
      Range.linear(1, 100)
    // Out initial state to be used for each test run
    , State(Map())
    // The list of commands
    , List(putCommand, getCommand)
    // Any cleanup code for the real-world, which in this case is to delete the KV file root for each test run
    , () => root.delete()
    )
}
```

This can be added to Hedgehog like any other property:

```scala
override def tests: List[Test] =
  List(
    property("test file KV (sequential)", testKVFileSequential)
  )
```

Some errors that a naive  file implementation found:

#### Invalid file path

```scala
> Var(Name(0)) = Put(???,b)

> Error thrown running execute: Invalid file path
```

The first set of line(s) are the series of commands that have been run in order
(ignore `Var` for now). Everything after that is our `Result` log.

So I'm generating invalid keys. So there is some restrictions on what our keys
can be. Interesting. For now update the generator to something much more
restrictive.

Note that this would have been found pretty quickly by our "normal" put/get
property. So far state-based testing hasn't done much for us.

Let's fix the key generator.

```scala
def genKey : Gen[String] =
  for {
    c <- Gen.lower
    s <- Gen.string(Gen.choice1(Gen.lower, Gen.constant('/')), Range.linear(0, 10))
  } yield c + s
```

#### Leaky File Abstraction

```scala
> Var(Name(15)) = Put(a,b)
> Var(Name(16)) = Get(a/)

> === Not Equal ===
> --- lhs ---
> None
> --- rhs ---
> Some(b)
```

Ahh, now we're getting somewhere a more interesting. We've generated a sequence
of putting to a key and then getting the same key with a trailing slash we get
back an unexpected value. The key-value abstraction has definitely leaked the
underlying file implementation!

This now would have required a slightly more sophisticated property testing
rather than a simple round-trip like shown above. We didn't even have to think
about it, it just comes with modelling each command once.

Also note that it appears to have found the absolute minimum case, where "a" is
the key. If you disable shrinking you may get up to 100 steps, where only two of
them are relevant to the failure case.

## Parallel

Ok, so this is where things get really interesting. By changing two lines we
transform our sequential test in to a parallel one. Without changing any of our
testing logic!

```scala
// Import this somewhere
import scala.concurrent.ExecutionContext.Implicits._

def testParallel: Property = {
  val root = new File("tmpdir")
  val kv = KV.file2(root)
  parallel(
    // This is still required, but now means the range of commands for the prefix (see below)
      Range.linear(1, 10)
    // This is now required. It specifies the range of command we want to generate for each parallel branch (see below)
    , Range.linear(1, 10)
    , State.default
    , commands(kv)
    , () => root.delete()
    )
}
```

What does it find?

```scala
> --- Prefix ---
Var(Name(1)) = Put(a,A)
--- Branch 1 ---
Var(Name(2)) = Put(a,A)
--- Branch 2 ---
Var(Name(3)) = Get(a)

> === Not Equal ===
> --- lhs ---
> Some(A)
> --- rhs ---
> Some()

> no valid interleaving
```

This can be slightly harder to read than sequential test output. In parallel
tests we start by generating a sequence of actions to be run in a single thread.
This is basically to initialise our state/store safely. At this point we then
generate two sequences of actions, and execute them on two JVM threads and race
them. At this point it's now basically impossible to observe the exact sequence
of actions that happened on either thread. Instead what hedgehog does is re-run
the model update  for each combination of actions, and at least one of the
expected results must not be a failure. We must be able to find one (potential)
way that we reached the output we observed (ie it must
[linearize](https://en.wikipedia.org/wiki/Linearizability))

From that error it looks like we are somehow getting back an empty result from
Get  That should be impossible, and yet here we are. If we look at the
implementation it should be easy to spot:

```scala
val w = new FileWriter(f)
w.write(v)
w.close()
```

There is a very small window where we open the file writer and write before
flushing/closing. And yet hedgehog found it! As a better alternative we could
write to a temporary file, and then rename once the data has been written, which
should be atomic.

Would we have found that edge-case ourselves with any kind of example or
property testing? I highly doubt it!

### Vars

One thing I have left-off is probably the most confusing aspect of the hedgehog
implementation of state-based testing (it doesn't exist in ScalaCheck for
example). This is that we have to deal with something called `Var` and
`Environment`. These are visible in the `execute`, `update` and `ensure`
functions:

```scala
def execute(env: Environment, i: Input): Either[String, Output]

def update(s: State, i: Input, o: Var[Output]): State

def ensure(env: Environment, s0: State, s1: State, i: Input, o: Output): Result
```

For the tests we just wrote we actually don't need them, but they are useful in
more complex testing scenarios. Please see the [next
tutorial](state-tutorial-vars.md) to learn more.

For now just ignore them, you can still get started and write some reasonable
properties like we just did.

## Resources

- The gold standard for an introduction to state-based testing is John Hughes
  - https://www.infoq.com/presentations/testing-techniques-case-study/
  - [Don't Write Tests](https://www.youtube.com/watch?v=hXnS_Xjwk2Y)
- https://teh.id.au/posts/2017/07/15/state-machine-testing/index.html
- https://github.com/qfpl/state-machine-testing-course
- [Introduction to stateful property based testing](https://www.youtube.com/watch?v=owHmYA52SIM)
- [State-based Testing Tutorial](https://www.youtube.com/watch?v=YhAxC_VI2dc)
