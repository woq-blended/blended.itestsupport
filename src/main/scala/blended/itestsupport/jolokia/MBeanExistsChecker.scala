package blended.itestsupport.jolokia

import akka.actor.{ActorSystem, Props}
import blended.itestsupport.condition.AsyncCondition
import blended.jolokia.{JolokiaClient, JolokiaObject, JolokiaSearchResult, MBeanSearchDef}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

object MBeanExistsCondition {

  def apply(
    url: String,
    user: Option[String] = None,
    pwd: Option[String] = None,
    searchDef: MBeanSearchDef,
    t : Option[FiniteDuration] = None
  )(implicit system: ActorSystem): AsyncCondition =
    AsyncCondition(
      Props(MBeanExistsChecker(url, user, pwd, searchDef)),
      s"MBeanExistsCondition($url, ${searchDef.pattern}})",
      t
    )
}

object CamelContextExistsCondition {
  def apply(
    url: String,
    user: Option[String] = None,
    pwd: Option[String] = None,
    contextName : String,
    t : Option[FiniteDuration] = None
  )(implicit system: ActorSystem): AsyncCondition = MBeanExistsCondition(
    url, user, pwd,
    MBeanSearchDef (
      jmxDomain = "org.apache.camel",
      searchProperties = Map(
        "type" -> "context",
        "name" -> s""""$contextName""""
      )
    )
  )
}

object JmsBrokerExistsCondition {
  def apply(
    url: String,
    user: Option[String] = None,
    pwd: Option[String] = None,
    brokerName : String,
    t : Option[FiniteDuration] = None
  )(implicit system: ActorSystem): AsyncCondition = MBeanExistsCondition(
    url, user, pwd,
    MBeanSearchDef (
      jmxDomain = "org.apache.activemq",
      searchProperties = Map(
        "type" -> "Broker",
        "brokerName" -> s""""$brokerName""""
      )
    )
  )
}

private[jolokia] object MBeanExistsChecker {
  def apply(
    url: String,
    user: Option[String] = None,
    pwd: Option[String] = None,
    searchDef: MBeanSearchDef
  ): MBeanExistsChecker = new MBeanExistsChecker(url, user, pwd, searchDef)
}

private[jolokia] class MBeanExistsChecker(
  url: String,
  userName: Option[String] = None,
  userPwd: Option[String] = None,
  searchDef : MBeanSearchDef
) extends JolokiaChecker(url, userName, userPwd) {

  override def toString: String = s"MbeanExistsCondition($url, ${searchDef.pattern}})"

  override def exec(client : JolokiaClient) : Try[JolokiaObject] = client.search(searchDef)

  override def assertJolokia (obj : Try[JolokiaObject]) : Boolean = obj match {
    case Success(r : JolokiaSearchResult) =>
      r.mbeanNames.nonEmpty
    case _ => false
  }
}
