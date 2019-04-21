package hedgehog

import hedgehog.core._

trait PropertyTOps extends PropertyTReporting {

  def point[A](value: A): PropertyT[A] =
    fromGen(Gen.constant(value))

  def fromGen[A](gen: GenT[A]): PropertyT[A] =
    PropertyT(gen.map(x => (Nil, Some(x))))

  def hoist[A](a: (List[Log], A)): PropertyT[A] =
    PropertyT(GenT.GenApplicative.point(a.copy(_2 = Some(a._2))))

  def writeLog(log: Log): PropertyT[Unit] =
    hoist((List(log), ()))

  def info(log: String): PropertyT[Unit] =
    writeLog(Info(log))

  def discard: PropertyT[Unit] =
    fromGen(Gen.discard)

  def failure: PropertyT[Unit] =
    failureA[Unit]

  def failureA[A]: PropertyT[A] =
    PropertyT(GenT.GenApplicative.point((Nil, None)))

  def error[A](e: Exception): PropertyT[A] =
    writeLog(Error(e)).flatMap(_ => failureA[A])

  def check(config: PropertyConfig, p: PropertyT[Result], seed: Seed): Report =
    propertyT.report(config, None, seed, p)

  def checkRandom(config: PropertyConfig, p: PropertyT[Result]): Report =
    // FIX: predef MonadIO
    check(config, p, Seed.fromTime())
}
