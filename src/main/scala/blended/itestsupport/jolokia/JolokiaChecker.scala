package blended.itestsupport.jolokia

import blended.itestsupport.condition.{AsyncChecker, AsyncCondition}
import blended.jolokia.{JolokiaAddress, JolokiaClient, JolokiaObject}

import scala.concurrent.Future
import scala.util.Try

abstract class JolokiaChecker(url: String, userName: Option[String], password: Option[String]) extends AsyncChecker {

  def exec(client : JolokiaClient) : Try[JolokiaObject]
  def assertJolokia(obj : Try[JolokiaObject]) : Boolean

  val client : JolokiaClient = new JolokiaClient(JolokiaAddress(
    jolokiaUrl = url, user = userName, password = password
  ))

  override def performCheck(condition: AsyncCondition): Future[Boolean] = Future {
    assertJolokia(exec(client))
  }
}
