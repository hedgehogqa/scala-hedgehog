---
title: 'Tutorial'
sidebar_label: 'Tutorial'
slug: '/guides-tutorial'
---
## Tutorial

- [Thanks](#thanks)
- [What is Hedgehog](#what-is-hedgehog)
- [Getting started](#getting-started)
  - [A quick example](#a-quick-example)
  - [Just a library](#just-a-library)
- [Properties](#properties)
  - [Results](#results)
    - [Logging](#logging)
    - [Combining](#combining)
  - [Generators](#generators)
    - [Case Classes](#case-classes)
    - [Lists](#lists)
    - [Filtering](#filtering)
    - [Sized](#sized)
  - [Shrinking](#shrinking)
  - [Classifications](#classifications)
- [State](#state)

## Thanks

**This guide was originally copied from the very excellent
[ScalaCheck Guide](https://github.com/rickynils/scalacheck/blob/master/doc/UserGuide.md)
and repurposed for Hedgehog**.


## What is Hedgehog?

Hedgehog is a tool for testing Scala and Java programs, based on property
specifications and automatic test data generation. The basic idea is that you
define a property that specifies the behaviour of a method or some unit of
code, and Hedgehog checks that the property holds. All test data are
generated automatically in a random fashion, so you don't have to worry about
any missed cases.


## Getting started

Please follow the general [getting started](getting-started.md)
guide first.


### A quick example


```scala
import hedgehog._
import hedgehog.runner._

object Spec extends Properties {

  override def tests: List[Test] =
    List(
      property("property", propConcatLists)
    )

  def propConcatLists: Property =
    for {
       l1 <- Gen.int(Range.linear(-100, 100)).list(Range.linear(0, 100)).forAll
       l2 <- Gen.int(Range.linear(-100, 100)).list(Range.linear(0, 100)).forAll
     } yield l1.size + l2.size ==== (l1 ::: l2).size
}
```

You can run this from `sbt`, either via `test` or an application  with `run`.

```
scala> test
+ Spec$.property: OK, passed 100 tests
scala> run Spec
+ Spec$.property: OK, passed 100 tests
```

OK, that seemed alright. Now define another property.

```scala
def propSqrt: Property =
  for {
    n <- Gen.int(Range.linearFrom(0, -100, 100)).forAll
  } yield scala.math.sqrt(n * n) ==== n
```

Check it!

```
- Spec$.property: Falsified after 8 passed tests
> -1
> === Not Equal ===
> --- lhs ---
> 1.0
> --- rhs ---
> -1.0
```

Not surprisingly, the property doesn't hold. The argument `-1` falsifies it.


### Just a library

Before we continue it's worth pointing out that for the most past Hedgehog is
_just_ a library. Let's run our first property directly using the API.


```scala
scala> import hedgehog._
scala> Property.checkRandom(Spec.propConcatLists).value
res0: hedgehog.core.Report = Report(SuccessCount(100),DiscardCount(0),OK)
```

We can see that the test output is returned as pure data.

Feel free to run the properties in this guide in any way you find most
convenient. We will continue to display the results from "running" the property,
just because it's more readable.


## Properties

There are two main concepts to Hedgehog are:

- [Results](#results)
- [Generators](#generators)

Let's start with the more simple and familar results and then move on to the
more interesting generators.


### Results

A `Result` is really a simple `Boolean` assertion with extra logging.
That's it (no really).

```scala
def testAdd: Result =
  Result.assert(1 + 2 == 2 + 1)
```

You can actually run these from Hedgehog like you would a full property.

```scala
object Spec extends Properties {

  override def tests: List[Test] =
    List(
      example("add", testAdd)
    )

  def testAdd: Result =
    Result.assert(1 + 2 == 2 + 1)
}
```

Note that we've used the `example` test function here instead of `property`,
which is used for the more powerful `Property` result.

And when we test it, notice that it only runs once and not 100 times.

```
+ Spec$.add: OK, passed 1 tests
```

This is just like any other test in ScalaTest/specs2/junit/etc.

What happens if we fail though?

```scala
def testAdd: Result =
  Result.assert(1 + 2 == 3 + 4)
```

```
Spec$.add: Falsified after 1 passed tests
```

That's it? What about a useful message telling us what failed?
For starters, given that we're just doing an assertion Hedgehog comes with the
convenient `====` operator:

```scala
def testAdd: Result =
  1 + 2 ==== 3 + 4
```

```
Spec$.testAdd: Falsified after 1 passed tests
> === Not Equal ===
> --- lhs ---
> 3
> --- rhs ---
> 7
```

That's a little better. But what happens if we don't just want to check
equality?

There is a method called `diff` which is similar to `Result.assert` but it gives the similar message to `====` operator's.

`diff` takes two arguments and the comparison function so that you can do any comparison operation you want on those two arguments.

```scala
def testAdd: Result =
  Result.diff(1 + 2, 3 + 4)(_ == _)
```

```
Spec$.testAdd: Falsified after 1 passed tests
> === Failed ===
> --- lhs ---
> 3
> --- rhs ---
> 7
```
*** 
```scala
def a1GtA2: Result =
  Result.diff(1 + 2, 3 + 4)(_ > _)
```

```
Spec$.a1GtA2: Falsified after 0 passed tests
> === Failed ===
> --- lhs ---
> 3
> --- rhs ---
> 7
```

If you want to change the log name (i.e. `=== Failed ===`) to something else, you can use `diffNamed` instead.

e.g.)

```scala
Result.diffNamed("=== Not Equal ===", 1 + 2, 3 + 4)(_ == _)
```
```
Spec$.testAdd: Falsified after 1 passed tests
> === Not Equal ===
> --- lhs ---
> 3
> --- rhs ---
> 7
```

In fact, `====` internally uses the `diffNamed` method.

#### Logging

Sometimes it can be difficult to decide exactly what is wrong when a property
fails, especially if the property is complex, with many conditions. In such
cases, you can log the different parts of the property, so Hedgehog can
tell you exactly what part is failing.

From the the example above, what happens if we wanted to check if two numbers
were less than each other?

```scala
def testTL: Result =
  Result.assert(2 < 1)
```

As we saw earlier, this wouldn't give a very useful error message.
This is where `log` comes in handy.

```scala
def testTL: Result =
  Result.assert(2 < 1)
    .log("2 is not less than 1")
```

We could ever make our own function to help re-use this.

```scala
def isLessThan(a: Int, b: Int): Result =
  Result.assert(a < b)
    .log(s"$a is not less than $b")

def testTL: Result =
  isLessThan(2, 1)
```

Where logging really comes in handy is when you start to use
[generators](#generators) and the results are different every time.

```scala
val complexProp: Property =
  for {
    m <- Gen.int(Range.linear(1, 100)).log("m")
    n <- Gen.int(Range.linear(1, 100)).log("n")
  } yield {
    val res = m + n
    Result.all(List(
      Result.assert(res >= m).log("result > #1")
    , Result.assert(res >= n).log("result > #2")
    , Result.assert(res < m + n).log("result not sum")
    ))
  }
```

```
- Spec.property: Falsified after 0 passed tests.
> m: 0
> n: 0
> result not sum
```

The log operator can also be used to inspect intermediate values
used in the properties, which can be very useful when trying to understand
why a property fails. Hedgehog always presents the generated property
arguments but sometimes you need to quickly see
the value of an intermediate calculation. See the following example, which
tries to specify multiplication in a somewhat naive way:

```scala
def propMul: Property =
  for {
    n <- Gen.int(Range.linear(1, 100)).log("n")
    m <- Gen.int(Range.linear(1, 100)).log("m")
  } yield {
    val res = n*m
    Result.all(List(
      (res / m ==== n).log("div1")
    , (res / n ==== m).log("div2")
    , Result.assert(res > m).log("lt1")
    , Result.assert(res > n).log("lt2")
    )).log("evidence = " + res)
  }
```

Here we have four different conditions, each with its own label. A fifth label
is added to the combined property to record the result of the multiplication.
When we check the property, Hedgehog tells us the following:

```
- Spec$.example: Falsified after 0 passed tests.
> n: 1
> n: 1
> lt1
> lt2
> evidence = 1
```

As you can see, you can add as many logs as you want to your result,
Hedgehog will only present the failing ones for the smallest example.


#### Combining

Results can be combined with other results into new ones using familiar
boolean logic.

```scala
def p1: Result =
  "a" ==== "a"

def p2: Result =
  1 ==== 1

def p3: Result =
  p1 and p2

def p4: Result =
  p1 or p2

// same as p1 and p2
def p5: Result =
  Result.all(List(p1, p2))

// same as p1 or p2
def p6: Result =
  Result.any(List(p1, p2))
```

Here, `p3` will hold if and only if both `p1` and `p2` hold, `p4` will hold if
either `p1` or `p2` holds.


### Generators

Generators are responsible for generating test data in Hedgehog, and are
represented by the `hedgehog.Gen` class. You need to know how to use this
class if you want Hedgehog to generate data of types that are not supported
by default to state properties about a specific subset of a type. In the `Gen`
object, there are several methods for creating new and modifying existing
generators. We will show how to use some of them in this section. For a more
complete reference of what is available, please see the Github source.

A generator can be seen simply as a function that takes some generation
parameters, and (maybe) returns a generated value. That is, the type `Gen[T]`
may be thought of as a function of type `Seed => Option[T]`. However, the
`Gen` class contains additional methods to make it possible to map generators,
use them in for-comprehensions and so on. Conceptually, though, you should
think of generators simply as functions, and the combinators in the `Gen`
object can be used to create or modify the behaviour of such generator
functions.

Let's see how to create a new generator. The best way to do it is to use the
generator combinators that exist in the `hedgehoge.Gen` module. These can
be combined using a for-comprehension. Suppose you need a generator which
generates a tuple that contains two random integer values, one of them being at
least twice as big as the other. The following definition does this:

```scala
val myGen: Gen[(Int, Int)] =
  for {
    n <- Gen.int(Range.linear(10, 20))
    m <- Gen.int(Range.linear(2*n, 500))
  } yield (n, m)
```

You can create generators that picks one value out of a selection of values.
The following generator generates a vowel:

```scala
def vowel: Gen[Char] =
  Gen.element1('A', 'E', 'I', 'O', 'U', 'Y')
```

The `element1` method creates a generator that randomly picks one of its
parameters each time it generates a value. Notice that plain values are
implicitly converted to generators (which always generates that value) if
needed.

The distribution is uniform, but if you want to control it you can use the
`frequency1` combinator:

```scala
def vowel: Gen[Char] =
  Gen.frequency1(
    (3, 'A')
  , (4, 'E')
  , (2, 'I')
  , (3, 'O')
  , (1, 'U')
  , (1, 'Y')
  )
```

Now, the `vowel` generator will generate `E`s more often than `Y`s. Roughly, 4/14
of the values generated will be `E`s, and 1/14 of them will be `Y`s.


#### Case Classes

It is very simple to generate random instances of case classes in Hedgehog.
Consider the following example where a binary integer tree is generated:

```scala
sealed abstract class Tree
case class Node(left: Tree, right: Tree, v: Int) extends Tree
case object Leaf extends Tree

val genLeaf: Gen[Tree] =
  Gen.constant(Leaf)

def genNode: Gen[Tree] =
  for {
    v <- Gen.int(Range.linear(-100, 100))
    left <- genTree
    right <- genTree
  } yield Node(left, right, v)

def genTree: Gen[Tree] =
  Gen.choice1(genLeaf, genNode)
```

We can now generate a sample tree:

```scala
def testTree: Property =
  forAll {
    t <- genTree.forAll
  } yield {
    println(t)
    Result.success
  }
```

```scala
Leaf
Node(Leaf,Leaf,-71)
Node(Node(Leaf,Leaf,-71),Node(Leaf,Leaf,-49),17),Leaf,-20
Node(Node(Node(Node(Node(Leaf,Leaf,-71),Node(Leaf,Leaf,-49),17),Leaf,-20),Leaf,-7),Node(Node(Leaf,Leaf,26),Leaf,-3),49)
Node(Leaf,Node(Node(Node(Node(Node(Node(Leaf,Leaf,-71),Node(Leaf,Leaf,-49),17),Leaf,-20),Leaf,-7),Node(Node(Leaf,Leaf,26),Leaf,-3),49),Leaf,84),-29)
```


#### Lists

There is a use generator, `list`, that generates a list of the current `Gen`.
You can use it in the following way:

```scala
def genIntList: Gen[List[Int]] =
  Gen.element1(1, 3, 5).list(Range.linear(0, 10))

def genBoolList: Gen[List[Boolean]] =
  Gen.constant(true).list(Range.linear(0, 10))

def genCharList: Gen[List[Char]] =
  Gen.alpha.list(Range.linear(0, 10))
```

It might be annoying to deal with a list of characters, which is where the
`Gen.string` function comes in handy.

```scala
def genStringList: Gen[String] =
  Gen.string(Gen.alpha, Range.linear(0, 10))
```


#### Filtering

Generator values can be restricted to ensure they meet some precondition.

```scala
val propMakeList: Property =
  for {
    n <- Gen.int(Range.linear(0, 100))
      .ensure(n => n % 2 == 0)
      .forAll
  } yield List.fill(n)("").length ==== n
}
```

Now Hedgehog will only care for the cases when `n` is even.

If `ensure` is given a condition that is hard or impossible to
fulfill, Hedgehog might not find enough passing test cases to state that the
property holds. In the following trivial example, all cases where `n` is
non-zero will be thrown away:

```scala
def propTrivial: Property =
  for {
    n <- Gen.int(Range.linear(0, 100))
     .ensure(n => n == 0)
     .forAll
  } yield n ==== 0
```

```
> Gave up after only 55 passed tests. 100 were discarded
```

It is possible to tell Hedgehog to try harder when it generates test cases,
but generally you should try to refactor your property specification instead of
generating more test cases, if you get this scenario.

Using `ensure`, we realise that a property might not just pass or fail, it
could also be undecided if the implication condition doesn't get fulfilled.


#### Sized

When Hedgehog uses a generator to generate a value, it feeds it with some
parameters. One of the parameters the generator is given, is a `Size` value,
which some generators use to generate their values. If you want to use the size
parameter in your own generator, you can use the `Gen.sized` method:

```scala
def matrix[T](g: Gen[T]): Gen[List[List[T]]] =
  Gen.sized(size => {
    val side = scala.math.sqrt(size.value).toInt
    g.list(Range.linear(0, side)).list(Range.linear(0, side))
  })
```

The `matrix` generator will use a given generator and create a matrix which
side is based on the generator size parameter. It uses the `list` function
which creates a sequence of given length filled with values obtained from the
given generator.


### Shrinking

In some ways the most interesting and important feature of Hedgehog is that if
it finds an argument that falsifies a property, it tries to _shrink_ that
argument before it is reported.

This is done automatically! This is crucially different from [QuickCheck] and
[ScalaCheck] which requires some hand-holding when it comes to shrinking.
We recommended watching the [original presentation](motivation.md)
for more information on how this works.

Let's look at specifying a property that says that no list has duplicate
elements in it. This is of course not true, but we want to see the test case
shrinking in action!

```scala
def p1: Property =
  for {
    l <- Gen.int(Range.linearFrom(0, -100, 100)).list(Range.linear(0, 100)).log("l")
  } yield l ==== l.distinct
```

Now, run the tests:

```
- Spec$.example: Falsified after 5 passed tests
> l: List(0,0)
> === Not Equal ===
> --- lhs ---
> List(0,0)
> --- rhs ---
> List(0)
```

Notice in particular the `i: List(0, 0)`, which captures the
smallest possible value that doesn't satisfy the invalid property.

Let's try that again, but let's see what else it tried.

```scala
def p1: Property =
  for {
    l <- Gen.int(Range.linearFrom(0, -100, 100)).list(Range.linear(0, 100)).log("i")
  } yield {
    println(l)
    l ==== l.distinct
  }
```

```scala
List()
List(1, -2)
List(-2, -3)
List(1, 4, 1)
List(0, 4, 1)
List(1, 0, 1)
List(1, 0, 0)
List()
List(0, 0)
List()
List(0)
```

You can see after a few tries Hedgehog finds an invalid example `List(1, 4, 1)`,
and starts to shrink both the values down to `0` and also the list size.

### Deterministic results

By default, Hedgehog uses a random seed that is based on the current system time. Normally, this is exactly what you want. However, if you have a failing test, the randomness of the generated test data can make it very difficult to reproduce and analyse the problem — especially if the test is only failing sporadically. In this situation, it would be better if you could get exactly the same generated test data that caused the test to fail.

This is why Hedgehog logs the seed together with the test results. In your console, you should see something like this:

```
Using random seed: 58973622580784
+ hedgehog.PropertyTest$.example1: OK, passed 1 tests
+ hedgehog.PropertyTest$.applicative: OK, passed 1 tests
+ hedgehog.PropertyTest$.applicative shrink: OK, passed 100 tests
```

Now imagine of these tests fails sporadically in your build pipeline. To analyse the problem locally, you can reproduce this test run by setting the seed to the same value. All you need to do is set the environment variable `HEDGEHOG_SEED` to the value in question.

Example:

```
export HEDGEHOG_SEED=58973622580784
```

Now you can reproduce the test run you're interested in. Hedgehog will inform you that it used the seed from the environment variable:

```
Using seed from environment variable HEDGEHOG_SEED: 58973622580784
+ hedgehog.PropertyTest$.example1: OK, passed 1 tests
+ hedgehog.PropertyTest$.applicative: OK, passed 1 tests
+ hedgehog.PropertyTest$.applicative shrink: OK, passed 100 tests
```


### Classifications

Using `classify` you can add classifications to your generator's data, for example:

```scala
  def testReverse: Property =
    for {
      xs <- Gen.alpha.list(Range.linear(0, 10)).forAll
         .classify("empty", _.isEmpty)
         .classify("nonempty", _.nonEmpty)
    } yield xs.reverse.reverse ==== xs
```

Running that property will produce a result like:

```
[info] + hedgehog.examples.ReverseTest.reverse: OK, passed 100 tests
[info] > 69% nonempty List(a)
[info] > 31% empty List()
```

Notice how, in addition to the percentage, it also presents a shrunk example for that classifier.

Using `cover` you may also specify a minimum coverage percentage for the given classification:

```scala
  def testReverse: Property =
    for {
      xs <- Gen.alpha.list(Range.linear(0, 10)).forAll
         .cover(50, "empty", _.isEmpty)
         .cover(50, "nonempty", _.nonEmpty)
    } yield xs.reverse.reverse ==== xs
```

```
[info] - hedgehog.examples.ReverseTest.reverse: Falsified after 100 passed tests
[info] > Insufficient coverage.
[info] > 93% nonempty 50% ✓ List(a)
[info] > 7% empty 50% ✗ List()
```

Finally:

* `label(name)` is an alias for `classify(name, _ => true)`, and
* `collect` is an alias for `labal` using the value's `toString` as the classification (label name)


## State

For a separate tutorial on state-based property testing please continue
[here](state-tutorial.md).
