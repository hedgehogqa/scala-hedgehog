package hedgehog.sbt

import hedgehog.core._
import hedgehog.runner._
import _root_.sbt.{testing => sbtt}

class Framework extends sbtt.Framework {

  override def name(): String =
    "Hedgehog"

  override def fingerprints(): Array[sbtt.Fingerprint] = {
    def mkFP(mod: Boolean, cname: String): sbtt.SubclassFingerprint =
      new sbtt.SubclassFingerprint {
        def superclassName(): String = cname
        val isModule: Boolean = mod
        def requireNoArgConstructor(): Boolean = true
      }

    Array(
      // For now only support classes. Do we want to have 2 different ways of doing the same thing?
      mkFP(false, classOf[Properties].getName)
    )
  }

  override def runner(args: Array[String], remoteArgs: Array[String], testClassLoader: ClassLoader): sbtt.Runner =
    new Runner(args, remoteArgs, testClassLoader)
}

class Runner(
    val args: Array[String]
  , val remoteArgs: Array[String]
  , testClassLoader: ClassLoader
  ) extends sbtt.Runner {

  override def tasks(taskDefs: Array[sbtt.TaskDef]): Array[sbtt.Task] =
    taskDefs.map(td =>
      try {
        Some(new Task(td, td.fingerprint.asInstanceOf[sbtt.SubclassFingerprint], testClassLoader))
      } catch {
        case e: ClassCastException =>
          None
      }
    ).flatMap(_.toList)

  override def done(): String =
    ""
}

class Task(
    val taskDef: sbtt.TaskDef
  , fingerprint: sbtt.SubclassFingerprint
  , testClassLoader: ClassLoader
  ) extends _root_.sbt.testing.Task {

  override def tags(): Array[String] =
    Array()

  override def execute(eventHandler: sbtt.EventHandler, loggers: Array[sbtt.Logger]): Array[sbtt.Task] = {
    val seed = Seed.fromTime()
    val c = testClassLoader
      .loadClass(taskDef.fullyQualifiedName)
      .asInstanceOf[Class[Properties]]
    val properties = c.newInstance
    properties.tests.foreach(t => {
      val startTime = System.currentTimeMillis
      val report = t.result.check(seed).value
      val endTime = System.currentTimeMillis
      eventHandler.handle(Event.fromReport(taskDef, new sbtt.TestSelector(t.name), report, endTime - startTime))

      loggers.foreach(logger => logger.info(renderReport(t, report, logger.ansiCodesSupported)))
    })
    Array()
  }

  def renderReport(t: Prop, report: Report, ansiCodesSupported: Boolean): String = {
    def render(ok: Boolean, msg: String, extraS: List[String]): String = {
      val name = taskDef.fullyQualifiedName + "." + t.name
      val sym = if (ok) "+" else "-"
      val colour = if (ok) Console.GREEN else Console.RED
      val extra = if (extraS.isEmpty) "" else "\n" + extraS.map(s => "> " + s).mkString("\n")
      if(ansiCodesSupported) {
        s"$colour$sym${Console.RESET} $name: $msg$extra"
      } else {
        s"$sym $name: $msg$extra"
      }
    }

    report.status match {
      case Failed(shrinks, log) =>
        render(false, s"Falsified after ${report.tests.value} passed tests", log.map(renderLog))
      case GaveUp =>
        render(false, s"Gave up after only ${report.tests.value} passed test. " +
        s"${report.discards.value} were discarded", Nil)
      case OK =>
        render(true, s"OK, passed ${report.tests.value} tests", Nil)
    }
  }

  def renderLog(log: Log): String =
    log match {
      case ForAll(name, value) =>
        s"${name.value}: $value"
      case Info(value) =>
        value
    }
}

case class Event(
   fullyQualifiedName: String
 , fingerprint: sbtt.Fingerprint
 , selector: sbtt.Selector
 , status: sbtt.Status
 , throwableO: Option[Throwable]
 , duration: Long
 ) extends sbtt.Event {

  override def throwable(): sbtt.OptionalThrowable =
    throwableO.fold(new sbtt.OptionalThrowable())(new sbtt.OptionalThrowable(_))
}

object Event {

  def fromReport(taskDef: sbtt.TaskDef, selector: sbtt.Selector, report: Report, duration: Long): Event = {
    val status = report.status match {
      case Failed(_, _) =>
        sbtt.Status.Failure
      case GaveUp =>
        sbtt.Status.Error
      case OK =>
        sbtt.Status.Success
    }
    Event(taskDef.fullyQualifiedName, taskDef.fingerprint, selector, status, None, duration)
  }
}
