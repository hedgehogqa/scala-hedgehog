package hedgehog

import hedgehog.state.Action._

import scala.concurrent._
import scala.concurrent.duration._

package object state {

  def sequential[S](
      range: Range[Int]
    , initial: S
    , commands: List[CommandIO[S]]
    , reset: () => Unit
    ): Property =
    genActions(range, commands, Context.create(initial)).map(_._2)
      .forAllWithLog(Runner.renderActions(_))
      .map(actions => {
        reset()
        executeSequential(initial, actions)
      })

  def parallel[S](
      prefixN: Range[Int]
    , parallelN: Range[Int]
    , initial: S
    , commands: List[CommandIO[S]]
    , reset: () => Unit
    )(implicit E: ExecutionContext): Property =
    genParallel(prefixN, parallelN, initial, commands)
      .forAllWithLog(Runner.renderParallel(_))
      .map(actions => {
        reset()
        Await.result(executeParallel(initial, actions), Duration.Inf)
      })
}
