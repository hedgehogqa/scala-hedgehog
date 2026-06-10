package hegehog.scalatest

private[scalacheck] object FailureMessages {

def decorateToStringValue(prettifier: org.scalactic.Prettifier, o: Any): String = prettifier.apply(o)

def propCheckLabel(): String = Resources.propCheckLabel()

def propCheckLabels(): String = Resources.propCheckLabels()

def propertyException(prettifier: org.scalactic.Prettifier, param0: Any): String = Resources.propertyException(prettifier.apply(param0))

def propCheckExhausted(prettifier: org.scalactic.Prettifier, param0: Any, param1: Any): String = Resources.propCheckExhausted(prettifier.apply(param0), prettifier.apply(param1))

def propCheckExhaustedAfterOne(prettifier: org.scalactic.Prettifier, param0: Any): String = Resources.propCheckExhaustedAfterOne(prettifier.apply(param0))

def propertyFailed(prettifier: org.scalactic.Prettifier, param0: Any): String = Resources.propertyFailed(prettifier.apply(param0))

def propertyCheckSucceeded(): String = Resources.propertyCheckSucceeded()

def thrownExceptionsLocation(prettifier: org.scalactic.Prettifier, param0: Any): String = Resources.thrownExceptionsLocation(prettifier.apply(param0))

def occurredOnValues(): String = Resources.occurredOnValues()

def thrownExceptionsMessage(prettifier: org.scalactic.Prettifier, param0: Any): String = Resources.thrownExceptionsMessage(prettifier.apply(param0))

def bigProblems(): String = Resources.bigProblems()

def bigProblemsWithMessage(prettifier: org.scalactic.Prettifier, param0: Any): String = Resources.bigProblemsWithMessage(prettifier.apply(param0))

def initSeed(prettifier: org.scalactic.Prettifier, param0: Any): String = Resources.initSeed(prettifier.apply(param0))

}
