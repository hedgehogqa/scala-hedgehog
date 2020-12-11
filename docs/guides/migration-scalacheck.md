---
title: 'Migration From ScalaCheck'
sidebar_label: 'Migration From ScalaCheck'
slug: '/guides-migration-from-scalacheck'
---
## Migration From ScalaCheck

For many cases migrating from ScalaCheck to Hedgehog should be _fairly_
straight forward, as the general principals are quite similar, and the changes
are largely syntactic.

- [Properties](#properties)
- [Arbitrary](#arbitary)
- [Gen](#gen)


## Properties

Some basic rules:

- Replace `Properties("...")` with just `Properties`
- Replace `Prop.forAll` with a call to [forAll] on a specific `Gen` instance
  - If you have previously been relying on `Arbitrary` instances these _have_
    to be replaced with calls to functions that return an instance of `Gen`.

    See the [extra] package for some stand Scala data type combinators.

    For more information see the section on [Gen](#gen).
- `flatMap` over the result of your `genFoo.forAll`, or use a `for`
   comprehension.
- Return your `Prop` or `Boolean` assetions with `Result.assert(...)`
- Replace [label] or `:|` with  [Result.log(...)][log]
- Replace equality assertions like `?=` with `====`


### ScalaCheck

```scala
import org.scalacheck._

object StringSpecification extends Properties("String") {

  property("startsWith") =
    Prop.forAll { (a: String, b: String) =>
      (a+b).startsWith(a)
    }
}
```

### Hedgehog

```scala
import hedgehog._
import hedgehog.runner._

object StringSpecification extends Properties {

  override def tests: List[Test] = List(
    property("startsWith", for {
      a <- Gen.string(Gen.unicode, Range.linear(0, 100)).forAll
      b <- Gen.string(Gen.unicode, Range.linear(0, 100)).forAll
      } yield Result.assert((a+b).startsWith(a))
    )
  )
}
```


## Gen

Some basic rules:

- `Gen.list` and `Gen.listOfN` can be replaced with a call to
  `list(Range.linear(0, n))` on a specific `Gen` instance.
- `Gen.const` is now `Gen.constant`
- `Arbitrary.arbitrary[Int]` is now `Gen.int(Range.linear(min, max))`
- `Gen.oneOf` is now `Gen.choice1`

It's important to note that there are no more "default" `Arbitrary` instances
to summon. You _must_ decided what kind of `int` or `String` you want to
generate, and what their `Range` is.

### ScalaCheck

```scala
val genLeaf = Gen.const(Leaf)

val genNode = for {
  v <- arbitrary[Int]
  left <- genTree
  right <- genTree
} yield Node(left, right, v)

def genTree: Gen[Tree] = Gen.oneOf(genLeaf, genNode)
```

### Hedgehog

```scala
val genLeaf = Gen.constant(Leaf)

val genNode = for {
  v <- Gen.int(Range.linear(Integer.MaxValue, Integer.MinValue))
  left <- genTree
  right <- genTree
} yield Node(left, right, v)

def genTree: Gen[Tree] = Gen.choice1(genLeaf, genNode)
```


## Arbitrary

Some basic rules:

- Replace `implict def` functions that return `Arbitrary` to a function
  that returns the `Gen` directly.


### ScalaCheck

This example was taken from the [ScalaCheck Guide].

```scala
implicit def arbTree[T](implicit a: Arbitrary[T]): Arbitrary[Tree[T]] = Arbitrary {

  val genLeaf = for(e <- Arbitrary.arbitrary[T]) yield Leaf(e)

  def genInternal(sz: Int): Gen[Tree[T]] = for {
    n <- Gen.choose(sz/3, sz/2)
    c <- Gen.listOfN(n, sizedTree(sz/2))
  } yield Internal(c)

  def sizedTree(sz: Int) =
    if(sz <= 0) genLeaf
    else Gen.frequency((1, genLeaf), (3, genInternal(sz)))

  Gen.sized(sz => sizedTree(sz))
}
```

```scala
def genTree[T](g: Gen[T]): Gen[Tree[T]] = {

  val genLeaf = for(e <- g) yield Leaf(e)

  def genInternal(sz: Size): Gen[Tree[T]] = for {
    n <- Gen.choose(sz.value/3, sz.value/2)
    c <- sizedTree(sz.value/2).list(Range.linear(0, n))
  } yield Internal(c)

  def sizedTree(sz: Size) =
    if(sz.value <= 0) genLeaf
    else Gen.frequency1((1, genLeaf), (3, genInternal(sz)))

  Gen.sized(sz => sizedTree(sz))
}
```


## Shrink

This is assuming you're even writing them in the first place...

### ScalaCheck

```scala
case class Data(a: String, i: Int)

implicit def arbData: Arbitrary[Data] =
  Arbitrary[Data] {
    for {
      s <- arbitrary[String]
      i <- arbitrary[Int]
    } yield Data(a, i)
  }

implicit def shrink: Shrink[Data] =
  Shrink[Data] { case Data(a, i) =>
    shrink(a).map(a2 => Data(a2, i)) append
    shrink(i).map(i2 => Data(a, i2))
  }
```

### Hedgehog

Good news, you don't need to do anything! Just write your generators.

```scala
def genData: Gen[Data] =
  for {
    s <- Gen.string(Gen.unicode, Range.linear(0, 100))
    i <- Gen.int(Range.linear(-100, 100))
  } yield Data(a, i)
```



[ScalaCheck]: https://www.scalacheck.org/
[ScalaCheck Guide]: https://github.com/rickynils/scalacheck/blob/master/doc/UserGuide.md#the-arbitrary-generator
[forAll]: https://github.com/hedgehogqa/scala-hedgehog/search?q=%22def+forAll%22&unscoped_q=%22def+forAll%22
[extra]: https://github.com/hedgehogqa/scala-hedgehog/tree/master/core/src/main/scala/hedgehog/extra
[label]: https://github.com/rickynils/scalacheck/search?q=%22def+label%22&unscoped_q=%22def+label%22
[log]: https://github.com/hedgehogqa/scala-hedgehog/search?q=%22def+log%22&unscoped_q=%22def+log%22
