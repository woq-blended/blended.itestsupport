package blended.itestsupport.condition

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._

/**
 * A Condition encapsulates an assertion that may change over time. The use case is to
 * wait for a Condition to be satisfied at some - normally that is a pre condition that
 * must be fulfilled before the real tests are executed.
 */
trait Condition {

  /** Is the condition satisfied ? */
  def satisfied   : Boolean
  val description : String

  /** The timeout a ConditionWaiter waits for this particular condition */
  def timeout   : FiniteDuration = defaultTimeout
  def interval  : FiniteDuration = defaultInterval

  lazy val config : Config = {
    val config = ConfigFactory.load()
    config.getConfig("blended.itestsupport.condition")
  }

  override def toString: String = s"Condition($description, $timeout)"

  private[this] def defaultTimeout : FiniteDuration = config.getLong("defaultTimeout").millis
  private[this] def defaultInterval : FiniteDuration = config.getLong("checkfrequency").millis
}
