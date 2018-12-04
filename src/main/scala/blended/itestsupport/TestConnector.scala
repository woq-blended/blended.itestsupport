package blended.itestsupport

import java.util.concurrent.ConcurrentHashMap

import blended.util.logging.Logger

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

object TestConnector {

  private val log : Logger = Logger[TestConnector.type]

  private val connectProperties : ConcurrentHashMap[String, Any] = new ConcurrentHashMap[String, Any]()

  def put(key : String, value : Any) : Unit = connectProperties.put(key, value)

  def property[T](key : String)(implicit clazz : ClassTag[T]) : Option[T] = {
    val result = Option(connectProperties.get(key)) match {
      case Some(t) if clazz.runtimeClass.isAssignableFrom(t.getClass) =>
        Some(t.asInstanceOf[T])
      case _ =>
        None
    }

    log.debug(s"TestConnector property [$key] is [$result]")
    result
  }

  def properties : Map[String, Any] = connectProperties.asScala.toMap
}

/**
  * Used to set up the test connector.
  */
trait TestConnectorSetup {
  def configure(cuts: Map[String, ContainerUnderTest]): Unit
}
