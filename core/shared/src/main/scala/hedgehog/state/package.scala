package hedgehog

import hedgehog.state.Action._

import scala.concurrent._
import scala.concurrent.duration._

package object state {

  def sequential[S](
      range: Range[Int]
    , initial: S
    , commands: List[CommandIO[S]]
    , cleanup: () => Unit
    ): Property =
    genActions(range, commands, Context.create(initial)).map(_._2)
      .forAllWithLog(Runner.renderActions(_))
      .map(actions =>
        try {
          executeSequential(initial, actions)
        } finally {
          cleanup()
        }
      )

  def parallel[S](
      prefixN: Range[Int]
    , parallelN: Range[Int]
    , initial: S
    , commands: List[CommandIO[S]]
    , cleanup: () => Unit
    )(implicit E: ExecutionContext): Property =
    genParallel(prefixN, parallelN, initial, commands)
      .forAllWithLog(Runner.renderParallel(_))
      .map(actions =>
        try {
          Await.result(executeParallel(initial, actions), Duration.Inf)
        } finally {
          cleanup()
        }
      )
}
