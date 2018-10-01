package hedgehog

import hedgehog.core._
import hedgehog.predef._

trait PropertyTOps[M[_]] extends PropertyTReporting[M] {

  def fromGen[A](gen: GenT[M, A])(implicit F: Monad[M]): PropertyT[M, A] =
    PropertyT(PropertyConfig.default, gen.map(x => (Nil, Some(x))))

  def hoist[A](a: (List[Log], A))(implicit F: Monad[M]): PropertyT[M, A] =
    PropertyT(PropertyConfig.default, GenT.GenApplicative(F).point(a.copy(_2 = Some(a._2))))

  def writeLog(log: Log)(implicit F: Monad[M]): PropertyT[M, Unit] =
    hoist((List(log), ()))

  def info(log: String)(implicit F: Monad[M]): PropertyT[M, Unit] =
    writeLog(Info(log))

  def discard(implicit F: Monad[M]): PropertyT[M, Unit] =
    fromGen(genT.discard)

  def failure(implicit F: Monad[M]): PropertyT[M, Unit] =
    PropertyT(PropertyConfig.default, GenT.GenApplicative(F).point((Nil, None)))

  def success(implicit F: Monad[M]): PropertyT[M, Unit] =
    hoist((Nil, ()))

  def assert(b: Boolean)(implicit F: Monad[M]): PropertyT[M, Unit] =
    if (b) success else failure

  def matchPattern[A](a: A)(right: PartialFunction[Any, _])(implicit F: Monad[M]): PropertyT[M, Unit] =
    // TODO: Think about a better way as this doesn't work if the pattern has guards
    if (right.isDefinedAt(a)) success else failure

}
