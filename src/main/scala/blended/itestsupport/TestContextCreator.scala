package blended.itestsupport

import akka.actor.{Actor, ActorLogging}
import akka.camel.CamelExtension
import akka.event.LoggingReceive
import org.apache.camel.CamelContext

class TestContextCreator extends Actor with ActorLogging { this: TestContextConfigurator =>
  import TestContextCreator._

  val camel = CamelExtension(context.system)

  def receive = LoggingReceive {
    case r: TestContextRequest =>
      log.info(s"Creating TestCamelContext for CUT's [${r.cuts}]")

      val result = try
        Right(configure(r.cuts, camel.context))
      catch {
        case t: Throwable => Left(t)
      }

      log debug s"Created TestCamelContext [$result]"

      sender ! TestContextResponse(result)
      context.stop(self)
  }

}

object TestContextCreator {

  /**
   * Use this to kick off the creation of a TestContext based on configured Containers under Test
   */
  case class TestContextRequest(cuts: Map[String, ContainerUnderTest])

  /**
   * This class returns a TestCamelContext that can be used for the integration tests or an Exception if
   * the context cannot be created
   */
  case class TestContextResponse(context: Either[Throwable, CamelContext])

}