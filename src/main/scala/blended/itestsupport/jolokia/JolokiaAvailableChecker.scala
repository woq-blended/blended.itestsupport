package blended.itestsupport.jolokia

import akka.actor.{ActorSystem, Props}
import blended.itestsupport.condition.AsyncCondition
import blended.jolokia.model.JolokiaVersion
import blended.jolokia.protocol._
import blended.util.logging.Logger

import scala.concurrent.duration.FiniteDuration

object JolokiaAvailableCondition {
  def apply(
    url: String,
    t: Option[FiniteDuration] = None,
    user: Option[String] = None,
    pwd: Option[String] = None
  )(implicit actorSys: ActorSystem) =
    AsyncCondition(Props(JolokiaAvailableChecker(url, user, pwd)), s"JolokiaAvailableCondition($url)", t)
}

private[jolokia] object JolokiaAvailableChecker {
  def apply(
    url: String,
    userName: Option[String] = None,
    userPwd: Option[String] = None
  ): JolokiaAvailableChecker = new JolokiaAvailableChecker(url, userName, userPwd)
}

private[jolokia] class JolokiaAvailableChecker(
  url: String,
  userName: Option[String] = None,
  userPwd: Option[String] = None
) extends JolokiaChecker(url, userName, userPwd) with JolokiaAssertion {

  private val log : Logger = Logger[JolokiaAvailableChecker]

  override def toString: String = s"JolokiaAvailableCondition($url)"

  override def jolokiaRequest : Any = GetJolokiaVersion

  override def assertJolokia: Any => Boolean = {
    case v: JolokiaVersion =>
      log.info(s"Jolokia [$v] discovered.")
      true
    case _ => false
  }
}
