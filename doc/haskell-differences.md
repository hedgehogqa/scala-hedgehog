Differences to Haskell Hedgehog
===============================

This page documents where the Scala Hedgehog API deviates significantly from the Haskell version.


## Result

The Haskell version allow for assertions throughout the `Property` monad, but the final value is
[()](https://github.com/hedgehogqa/haskell-hedgehog/blob/694d3648f808d2401834c3e75db24b960ee8a68c/hedgehog/src/Hedgehog/Internal/Property.hs#L133).

```haskell
prop_reverse :: Property
prop_reverse =
  property $ do
    xs <- forAll $ Gen.list (Range.linear 0 100) Gen.alpha
    reverse (reverse xs) === xs
```

And the corresponding Scala version:

```scala
def propReverse: Property =
  for {
    xs <- Gen.alpha.list(Range.linear(0, 100)).forAll
  } yield xs.reverse.reverse === xs
```

### Resource Management

This approach makes it more difficult to isolate resource management in a strict language like Scala.
It then becomes fairly important in the Haskell version to use
[ResourceT](https://github.com/hedgehogqa/haskell-hedgehog/blob/master/hedgehog-example/src/Test/Example/Resource.hs):

```haskell
prop_unix_sort :: Property
prop_unix_sort =
  property $ do
    values0 <- forAll $ Gen.list (Range.linear 0 100) Gen.alpha
    test . runResourceT $ do
      dir <- Temp.createTempDirectory Nothing "prop_dir"
      ...
      values0 === values
```

To simplify this, and to reduce surprises, the final result in the Scala version is now a separate
[Result](https://github.com/hedgehogqa/scala-hedgehog/blob/master/core/src/main/scala/hedgehog/core/Result.scala) value,
which forces a single, final assertion to be returned.

```scala
def propUnixSort: Property =
  for {
    values0 <- Gen.alpha.list(Range.linear(0, 100)).forAll
  } yield {
    val dir = java.io.Files.createTempDirectory(getClass.getSimpleName).toFile
    try {
      values0 === values
    } finally {
      dir.delete()
    }
  }
```
