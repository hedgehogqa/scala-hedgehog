package hedgehog.sbt

import hedgehog._
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
      mkFP(false, classOf[Properties].getName)
    , mkFP(true, classOf[Properties].getName)
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
    val config = PropertyConfig.default
    val seed = Seed.fromTime()
    val c = testClassLoader
      .loadClass(taskDef.fullyQualifiedName + (if (fingerprint.isModule) "$" else ""))
      .asInstanceOf[Class[Properties]]
    val properties =
      if (fingerprint.isModule)
        // FIX Use scala-reflect to be more future compatible
        c.getField("MODULE$").get(c).asInstanceOf[Properties]
      else
        c.getDeclaredConstructor().newInstance()
    properties.tests.foreach(t => {
      val startTime = System.currentTimeMillis
      val report = Property.check(t.withConfig(config), t.result, seed).value
      val endTime = System.currentTimeMillis
      eventHandler.handle(Event.fromReport(taskDef, new sbtt.TestSelector(t.name), report, endTime - startTime))

      loggers.foreach(logger => logger.info(Test.renderReport(taskDef.fullyQualifiedName, t, report, logger.ansiCodesSupported)))
    })
    Array()
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
