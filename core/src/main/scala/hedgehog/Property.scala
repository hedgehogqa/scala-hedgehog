package hedgehog

import scalaz._, Scalaz._
import scalaz.effect._

sealed trait Break
case object Failure extends Break
case object Discard extends Break

case class Shrinks(value: Int)

sealed trait Status
case class Failed(shrinks: Shrinks, errors: List[String]) extends Status
case object GaveUp extends Status
case object OK extends Status

object Status {

  def failed(s: Shrinks, e: List[String]): Status =
    Failed(s, e)

  val gaveUp: Status =
    GaveUp

  val ok: Status =
    OK
}

case class Report(tests: Int, discards: Int, status: Status)

/**
 * NOTE: Type inference goes to shit when we do the proper transformer encoding.
 *
 *   case class Property[M[_], A](run: Gen[EitherT[WriterT[Tree[M, ?], List[String], ?], Break, ?], A]) {
 */
case class Property[M[_], A](run: Gen[Tree[M, ?], (List[String], Break \/ A)]) {

  def runTree(implicit F: Functor[M]): Gen[M, Node[M, (List[String], Break \/ A)]] =
    Gen(s => run.run(s).run.map(n =>
      // TODO Discard seeds here, can never be good
      (n.value._1, Node(n.value._2, n.children.map(_.map(_._2))))
    ))

  def map[B](f: A => B)(implicit F: Functor[M]): Property[M, B] =
    Property(run.map(_.map(_.map(f))))

  def flatMap[B](f: A => Property[M, B])(implicit F: Monad[M]): Property[M, B] =
    Property(run.flatMap(x => x._2.fold(l =>
        GenTree.applicative(F).point((x._1, l.left[B]))
      , w => f(w).run.map(y => (x._1 ++ y._1, y._2))
    )))
}

object Property {

  implicit def PropertyMonad[M[_]](implicit F: Monad[M]): Monad[Property[M, ?]] =
    new Monad[Property[M, ?]] {
      override def map[A, B](fa: Property[M, A])(f: A => B): Property[M, B] =
        fa.map(f)
      override def point[A](a: => A): Property[M, A] =
        hoist((Nil, a.right))
      override def bind[A, B](fa: Property[M, A])(f: A => Property[M, B]): Property[M, B] =
        fa.flatMap(f)
    }

  def fromGenTree[M[_] : Monad, A](gen: Gen[Tree[M, ?], A]): Property[M, A] =
    Property(gen.map(x => (Nil, x.right)))

  def hoist[M[_] : Monad, A](a: (List[String], Break \/ A))(implicit F: Monad[M]): Property[M, A] =
    Property(GenTree.applicative(F).point(a))

  def forAll[M[_] : Monad, A](gen: Gen[Tree[M, ?], A]): Property[M, A] =
    for {
      x <- fromGenTree(gen)
      // TODO Add better render, although I don't really like Show
      _ <- counterexample[M](x.toString)
    } yield x

  def counterexample[M[_] : Monad](value: String): Property[M, Unit] =
    hoist((List(value), ().right))

  def discard[M[_] : Monad]: Property[M, Unit] =
    hoist((Nil, Discard.left))

  def failure[M[_] : Monad, A]: Property[M, A] =
    hoist((Nil, Failure.left))

  def success[M[_] : Monad]: Property[M, Unit] =
    hoist((Nil, ().right))

  def assert[M[_] : Monad](b: Boolean): Property[M, Unit] =
    if (b) success else failure

  def isFailure[M[_], A, B](n: Node[M, (B, Break \/ A)]): Boolean =
    n.value._2 == Failure.left

  def takeSmallest[M[_] : Monad](s: Shrinks, n: Node[M, (List[String], Break \/ Unit)]): M[Status] =
    n.value._2 match {
      case -\/(Failure) =>
        implicitly[Foldable[List]]
          .findMapM[M, Tree[M, (List[String], Break \/ Unit)], Status](n.children)(m =>
            m.run.flatMap(node =>
              if(isFailure(node))
                takeSmallest(Shrinks(s.value + 1), node).map(some)
              else
                none.point[M]
            )
          )
          .map(_.getOrElse(Status.failed(s, n.value._1)))

      case -\/(Discard) =>
        Status.gaveUp.point[M]

      case \/-(()) =>
        Status.ok.point[M]
    }

  def report[M[_] : Monad](n : Int, p: Property[M, Unit]): Gen[M, Report] = {
    def loop(tests: Int, discards: Int): Gen[M, Report] =
      if (tests == n)
        Report(tests, discards, OK).point[Gen[M, ?]]
      else if (tests >= 100)
        Report(tests, discards, GaveUp).point[Gen[M, ?]]
      else
        p.runTree.flatMap(x => x.value._2 match {
          case -\/(Failure) =>
            Gen.lift(takeSmallest(Shrinks(0), x).map(y => Report(tests, discards, y)))

          case -\/(Discard) =>
            loop(tests, discards + 1)

          case \/-(_) =>
            loop(tests + 1, discards)
        })
    loop(0, 0)
  }

  def check[M[_] : Monad](seed: Long)(p: Property[M, Unit]): M[Report] =
    report(100, p).run(Seed.fromSeed(seed)).map(_._2)

  def checkRandom[M[_] : MonadIO](p: Property[M, Unit]): M[Report] =
    Seed.fromTime.liftIO.flatMap(s =>
      report(100, p).run(s).map(_._2)
    )
}
