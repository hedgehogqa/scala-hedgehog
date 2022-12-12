package hedgehog.state

import hedgehog.predef._

import scala.collection.immutable.SortedSet


/**
 * Variables are the symbolic representation of executing an action.
 * To lookup the actual result of an action you require an `Environment`,
 * which is only accessible at specific stages of testing.
 *
 * The constructor is hidden to avoid users accidentally creating their own.
 */
case class Var[A] private[state](name: Name) {

  def get(env: Environment): A =
    // Silently throw the error, this will only happen if the user doesn't implement `vars` correctly
    // This avoids having to force users to return the (unlikely) EnvironmentError
    env.reify(this).fold(e => throw e, identity)
}

object Var {

  /** Insert a symbolic variable in to a map of variables to types. */
  def insert[A](m: SortedSet[Name], s: Var[A]): SortedSet[Name] =
    m + s.name

  def takeVariables(vs: List[Var[_]]): SortedSet[Name] =
    State.traverse(vs)(v =>
      State.modify[SortedSet[Name]](m => insert(m, v))
        .map(_ => v)
    ).exec(SortedSet.empty[Name]).value

  def variablesOK(xs: List[Var[_]], allowed: SortedSet[Name]): Boolean =
    takeVariables(xs).forall(allowed.contains)

}

/** Symbolic variable names. */
case class Name(value: Int)

object Name {

  implicit val NameOrdering: Ordering[Name] =
    Ordering.by(_.value)
}

case class Context[S](state: S, vars: SortedSet[Name])

object Context {

  def newVar[S, A](c: Context[S]): (Context[S], Var[A]) = {
    val v: Var[A] = c.vars.lastOption match {
      case None =>
        Var(Name(0))
      case Some(name) =>
        Var(Name(name.value + 1))
    }
    (c.copy(vars = Var.insert[A](c.vars, v)), v)
  }

  def create[S](s: S): Context[S] =
    Context(s, SortedSet())
}

/** A mapping of symbolic values to concrete values. */
case class Environment(value: Map[Name, Any]) {

  def reify[A](n: Var[A]): Either[EnvironmentError, A] =
    value.get(n.name)
      .toRight(EnvironmentError.valueNotFound(n.name))
      .rightFlatMap(dyn =>
        try {
          Right(dyn.asInstanceOf[A])
        } catch {
          case e: ClassCastException =>
            Left(EnvironmentError.TypeError(n.name, dyn, e))
        }
      )
}

sealed trait EnvironmentError extends Exception

object EnvironmentError {

  case class ValueNotFound(name: Name) extends EnvironmentError

  case class TypeError(name: Name, d: Any, e: ClassCastException) extends EnvironmentError

  def valueNotFound(name: Name): EnvironmentError =
    ValueNotFound(name)

  def typeError(name: Name, d: Any, e: ClassCastException): EnvironmentError =
    TypeError(name, d, e)
}
