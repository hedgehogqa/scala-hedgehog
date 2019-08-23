package hedgehog.core

/** Whether a test is covered by a classifier, and therefore belongs to a `Class` */
sealed trait Cover {

  def ++(o: Cover): Cover =
    this match {
      case Cover.NoCover =>
        o match {
          case Cover.NoCover =>
            Cover.NoCover
          case Cover.Cover =>
            Cover.Cover
        }
      case Cover.Cover =>
        Cover.Cover
    }
}

object Cover {

  case object NoCover extends Cover
  case object Cover extends Cover

  implicit def Boolean2Cover(b: Boolean): Cover =
    if (b) Cover else NoCover
}

/** The total number of tests which are covered by a classifier. */
case class CoverCount(toInt: Int) {

  def +(o: CoverCount): CoverCount =
    CoverCount(toInt + o.toInt)

  def percentage(tests: SuccessCount): CoverPercentage =
    CoverPercentage(((toInt.toDouble / tests.value.toDouble) * 100 * 10).round / 10)
}

object CoverCount {

  def fromCover(c: Cover): CoverCount =
    c match {
      case Cover.NoCover =>
        CoverCount(0)
      case Cover.Cover =>
        CoverCount(1)
    }
}

/** The relative number of tests which are covered by a classifier. */
case class CoverPercentage(toDouble: Double)

object CoverPercentage {

  implicit def Double2CoveragePercentage(d: Double): CoverPercentage =
    CoverPercentage(d)
}

/** The name of a classifier. */
case class LabelName(render: String)

object LabelName {

  implicit def String2LabelName(s: String): LabelName =
    LabelName(s)
}

/**
 * The extent to which a test is covered by a classifier.
 *
 * _When a classifier's coverage does not exceed the required minimum, the test will be failed._
 */
case class Label[A](
    name : LabelName
  , minimum : CoverPercentage
  , annotation : A
  )

object Label {

  def covered(label: Label[CoverCount], tests: SuccessCount): Boolean =
    label.annotation.percentage(tests).toDouble >= label.minimum.toDouble
}

case class Coverage[A](labels: Map[LabelName, Label[A]])

object Coverage {

  def empty[A]: Coverage[A] =
    Coverage(Map.empty[LabelName, Label[A]])

  def count(cv: Coverage[Cover]): Coverage[CoverCount] =
    cv.copy(labels = cv.labels.map { case (k, l) =>
      k -> l.copy(annotation = CoverCount.fromCover(l.annotation))
    })

  def union[A](a: Coverage[A], b: Coverage[A])(append: (A, A) => A): Coverage[A] =
    Coverage(b.labels.toList.foldLeft(a.labels) { case (m, (k, v)) =>
      m + (k -> m.get(k).map(x => x.copy(annotation = append(x.annotation, v.annotation))).getOrElse(v))
    })

  def split(coverage: Coverage[CoverCount], tests: SuccessCount): (List[Label[CoverCount]], List[Label[CoverCount]]) = {
    val (c, un) = coverage.labels.values.partition(Label.covered(_, tests))
    (c.toList, un.toList)
  }
}

