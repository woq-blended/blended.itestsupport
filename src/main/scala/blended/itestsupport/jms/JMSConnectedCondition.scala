package blended.itestsupport.jms

import akka.actor.{ActorSystem, Props}
import blended.itestsupport.condition.AsyncCondition
import blended.itestsupport.jolokia.JolokiaChecker
import blended.jolokia.{JolokiaClient, JolokiaObject, JolokiaReadResult, MBeanSearchDef}
import blended.util.logging.Logger

import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

object JMSConnectedCondition {

  def apply(
    jmxUrl : String,
    jmxUser: Option[String], jmxPwd: Option[String],
    vendor: String,
    provider: String,
    t : Option[FiniteDuration] = None
  )(implicit system: ActorSystem) = AsyncCondition(
    Props(new JMSConnectedChecker(jmxUrl, jmxUser, jmxPwd, vendor, provider)),
    s"JMSConnectedChecker($vendor, $provider)",
    t
  )

}

private[jms] class JMSConnectedChecker(
  url : String,
  user : Option[String],
  pwd: Option[String],
  vendor : String,
  provider : String
) extends JolokiaChecker(url, user, pwd) {

  private val log : Logger = Logger[JMSConnectedChecker]

  override def exec(client: JolokiaClient): Try[JolokiaObject] = Try {
    client.search(MBeanSearchDef(
      jmxDomain = "blended",
      searchProperties = Map(
        "type" -> "ConnectionMonitor",
        "vendor" -> vendor,
        "provider" -> provider
      )
    )).map { _.mbeanNames match {
      case h :: _ => client.read(h).get
      case Nil => throw new Exception(s"MBean for [$vendor:$provider] not found")
    }}.get
  }

  override def assertJolokia(obj: Try[JolokiaObject]): Boolean = obj match {
    case Success(r : JolokiaReadResult) =>
      val stat = r.attributes.get("Status")

      log.debug(s"Status for connection [$vendor:$provider] is [$stat]")
      stat.map(_.toString()).contains("true")
    case _ => false
  }
}