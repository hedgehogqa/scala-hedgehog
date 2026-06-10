package hegehog.scalatest

import java.text.MessageFormat
import java.util.ResourceBundle

private[scalacheck] object Resources {

lazy val resourceBundle = ResourceBundle.getBundle("org.scalatestplus.scalacheck.MessageBundle")

def makeString(resourceName: String, args: Array[Any]): String = {
  val raw = resourceBundle.getString(resourceName)
  formatString(raw, args)
}

def formatString(rawString: String, args: Array[Any]): String = {
  val msgFmt = new MessageFormat(rawString)
  msgFmt.format(args.toArray)
}

def propCheckLabel(): String = resourceBundle.getString("propCheckLabel")

def rawPropCheckLabel: String = resourceBundle.getString("propCheckLabel")

def propCheckLabels(): String = resourceBundle.getString("propCheckLabels")

def rawPropCheckLabels: String = resourceBundle.getString("propCheckLabels")

def propertyException(param0: Any): String = makeString("propertyException", Array(param0))

def rawPropertyException: String = resourceBundle.getString("propertyException")

def propCheckExhausted(param0: Any, param1: Any): String = makeString("propCheckExhausted", Array(param0, param1))

def rawPropCheckExhausted: String = resourceBundle.getString("propCheckExhausted")

def propCheckExhaustedAfterOne(param0: Any): String = makeString("propCheckExhaustedAfterOne", Array(param0))

def rawPropCheckExhaustedAfterOne: String = resourceBundle.getString("propCheckExhaustedAfterOne")

def propertyFailed(param0: Any): String = makeString("propertyFailed", Array(param0))

def rawPropertyFailed: String = resourceBundle.getString("propertyFailed")

def propertyCheckSucceeded(): String = resourceBundle.getString("propertyCheckSucceeded")

def rawPropertyCheckSucceeded: String = resourceBundle.getString("propertyCheckSucceeded")

def thrownExceptionsLocation(param0: Any): String = makeString("thrownExceptionsLocation", Array(param0))

def rawThrownExceptionsLocation: String = resourceBundle.getString("thrownExceptionsLocation")

def occurredOnValues(): String = resourceBundle.getString("occurredOnValues")

def rawOccurredOnValues: String = resourceBundle.getString("occurredOnValues")

def thrownExceptionsMessage(param0: Any): String = makeString("thrownExceptionsMessage", Array(param0))

def rawThrownExceptionsMessage: String = resourceBundle.getString("thrownExceptionsMessage")

def bigProblems(): String = resourceBundle.getString("bigProblems")

def rawBigProblems: String = resourceBundle.getString("bigProblems")

def bigProblemsWithMessage(param0: Any): String = makeString("bigProblemsWithMessage", Array(param0))

def rawBigProblemsWithMessage: String = resourceBundle.getString("bigProblemsWithMessage")

def initSeed(param0: Any): String = makeString("initSeed", Array(param0))

def rawInitSeed: String = resourceBundle.getString("initSeed")

def bigProblems(ex: Throwable): String = {
  val message = if (ex.getMessage == null) "" else ex.getMessage.trim
  if (message.length > 0) Resources.bigProblemsWithMessage(message) else Resources.bigProblems()
}
}
    