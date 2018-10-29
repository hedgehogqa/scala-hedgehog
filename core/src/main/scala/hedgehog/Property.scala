package hedgehog

import hedgehog.core._
import hedgehog.predef._

trait PropertyTOps[M[_]] extends PropertyTReporting[M] {

  def point[A](value: A)(implicit F: Monad[M]): PropertyT[M, A] =
    fromGen(genT.constant(value))

  def fromGen[A](gen: GenT[M, A])(implicit F: Monad[M]): PropertyT[M, A] =
    PropertyT(gen.map(x => (Nil, Some(x))))

  def hoist[A](a: (List[Log], A))(implicit F: Monad[M]): PropertyT[M, A] =
    PropertyT(GenT.GenApplicative(F).point(a.copy(_2 = Some(a._2))))

  def writeLog(log: Log)(implicit F: Monad[M]): PropertyT[M, Unit] =
    hoist((List(log), ()))

  def info(log: String)(implicit F: Monad[M]): PropertyT[M, Unit] =
    writeLog(Info(log))

  def discard(implicit F: Monad[M]): PropertyT[M, Unit] =
    fromGen(genT.discard)

  def failure(implicit F: Monad[M]): PropertyT[M, Unit] =
    failureA[Unit]

  def failureA[A](implicit F: Monad[M]): PropertyT[M, A] =
    PropertyT(GenT.GenApplicative(F).point((Nil, None)))

  def error[A](e: Exception)(implicit F: Monad[M]): PropertyT[M, A] =
    writeLog(Error(e)).flatMap(_ => failureA[A])

  def check(config: PropertyConfig, p: PropertyT[M, Result], seed: Seed)(implicit F: Monad[M]): M[Report] =
    propertyT.report(config, Size(0), seed, p)

  def checkRandom(config: PropertyConfig, p: PropertyT[M, Result])(implicit F: Monad[M]): M[Report] =
    // FIX: predef MonadIO
    check(config, p, Seed.fromTime())
}
