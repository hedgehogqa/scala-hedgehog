package hedgehog.predef

case class OptionT[M[_], A](run: M[Option[A]])

abstract class OptionTImplicits1 {

  implicit def OptionTFunctor[M[_]](implicit F: Functor[M]): Functor[OptionT[M, ?]] =
    new Functor[OptionT[M, ?]] {

      override def map[A, B](fa: OptionT[M, A])(a: A => B): OptionT[M, B] =
        {
          println(F)
          ???
        }
    }
}

object OptionT extends OptionTImplicits1 {

  implicit def OptionTMonad[M[_]](implicit F: Monad[M]): Monad[OptionT[M, ?]] =
    new Monad[OptionT[M, ?]] {

      override def point[A](a: => A): OptionT[M, A] =
        {
        println(F)
        ???
        }

      override def bind[A, B](fa: OptionT[M, A])(f: A => OptionT[M, B]): OptionT[M, B] =
        ???
    }
}
