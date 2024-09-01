---
id: 'motivation'
title: 'Motivation'
sidebar_position: 2
sidebar_label: 'Motivation'
slug: '/motivation'
---
## Motivation

The background and motivation for Hedgehog in general is still best described by the original
author in this excellent presenation:

<iframe width="640" height="360" src="https://www.youtube.com/embed/LfD0DHqpeVQ" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

- Watch it on Youtube: [Gens N’ Roses: Appetite for Reduction](https://www.youtube.com/watch?v=LfD0DHqpeVQ) ([slides](https://slides.yowconference.com/yowlambdajam2017/Stanley-GensNRoses.pdf))

A very quick summary is that the original QuickCheck and it's derivatives (like ScalaCheck)
separate the generation of data from the shrinking, which results in something that cannot be
composed easily. It turns out it's fairly simple to combine them in a single data-type.

If you've used ScalaCheck before, it's exactly the same as writing your normal `Gen` functions,
but now those generated value will shrink without any extra information. Magic!


## Design Considerations


As a general rule, the current Scala API is intended to be _direct_ port of
[haskell-hedgehog](https://github.com/hedgehogqa/haskell-hedgehog), much like
[scalacheck](https://github.com/typelevel/scalacheck) was for [QuickCheck](http://hackage.haskell.org/package/QuickCheck).
The idea being that people familiar with one of the libraries will be comfortable with the other.
It also makes it easier not having to re-invent any wheels (or APIs).
There will obviously be exceptions where Scala forces us to make a different trade-off.
See [haskell-differences](../guides/haskell-differences.md) for examples and more explanation.
