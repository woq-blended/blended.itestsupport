package blended.itestsupport

import java.io.{ByteArrayOutputStream, File, FileOutputStream}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration

import akka.actor.ActorRef
import akka.pattern.ask
import akka.testkit.{TestKit, TestProbe}
import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import blended.itestsupport.BlendedTestContextManager.{ConfiguredContainer, ConfiguredContainer_?, ContainerReady, ContainerReady_?}
import blended.itestsupport.TestContextCreator.TestContextRequest
import blended.itestsupport.compress.TarFileSupport
import blended.itestsupport.condition.ConditionActor.CheckCondition
import blended.itestsupport.condition.ConditionActor.ConditionCheckResult
import blended.itestsupport.condition.{Condition, ConditionActor}
import blended.itestsupport.docker.protocol._
import blended.util.logging.Logger
import org.apache.camel.CamelContext

trait BlendedIntegrationTestSupport {

  private[this] val logger = Logger[BlendedIntegrationTestSupport]

  val testOutput: String = System.getProperty("projectTestOutput", "")

  def testContext(ctProxy: ActorRef)(implicit timeout: Timeout, testKit: TestKit): CamelContext = {
    val probe = new TestProbe(testKit.system)
    val cuts = ContainerUnderTest.containerMap(testKit.system.settings.config)
    ctProxy.tell(TestContextRequest(cuts), probe.ref)
    probe.receiveN(1, timeout.duration).head.asInstanceOf[CamelContext]
  }

  def containerReady(ctProxy: ActorRef)(implicit timeout: Timeout, testKit: TestKit): Unit = {
    val probe = new TestProbe(testKit.system)
    ctProxy.tell(ContainerReady_?, probe.ref)
    // TODO: instead of just expecting the success, we should get the whole status
    // to provide a much better error message about WHICH condition failed.
    try {
      //      probe.expectMsg(timeout.duration, ContainerReady(true))
      probe.expectMsgPF(timeout.duration) {
        case ContainerReady(true, _, _) => // all good
        case ContainerReady(false, good, bad) => // bad
          throw new AssertionError(s"The container is not ready. Reasons:\n  ${bad.mkString("\n  ")}")
      }
    } catch {
      case e: AssertionError =>
        logger.error(e)("Container setup didn't finish successfully within timeout.\n" +
          "To get better error logging, reduce the timeout of the conditions and/or increase the timeout of this `containerReady` call.")
        val extendedStatus = "\nRefer to log file to find out which condition failed."
        throw new AssertionError(e.getMessage() + extendedStatus, e.getCause())
    }
  }

  def stopContainers(ctProxy: ActorRef)(implicit timeout: Timeout, testKit: TestKit): Unit = {
    val probe = new TestProbe(testKit.system)
    testKit.system.log.debug(s"stopProbe [${probe.ref}]")
    ctProxy.tell(StopContainerManager(timeout.duration), probe.ref)
    probe.expectMsg(timeout.duration, ContainerManagerStopped)
  }

  def writeContainerDirectory(
    ctProxy: ActorRef,
    ctName: String,
    target: String,
    file: File,
    user: Int = 0,
    group: Int = 0
  )(implicit timeout: Timeout, testKit: TestKit): Future[WriteContainerDirectoryResult] = {

    logger.info(s"Writing directory [${file.getAbsolutePath()}] to [$ctName:$target]")
    implicit val eCtxt = testKit.system.dispatcher

    val bos = new ByteArrayOutputStream()
    TarFileSupport.tar(file, bos, user, group)

    ctProxy.ask(ConfiguredContainer_?(ctName)).mapTo[ConfiguredContainer].flatMap { cc =>
      cc.cut match {
        case None => Future(WriteContainerDirectoryResult(Left(new Exception(s"Container with name [$ctName] not found."))))
        case Some(cut) => ctProxy.ask(WriteContainerDirectory(cut, target, bos.toByteArray())).mapTo[WriteContainerDirectoryResult]
      }
    }
  }

  def readContainerDirectory(ctProxy: ActorRef, ctName: String, dirName: String)(implicit timeout: Timeout, testKit: TestKit): Future[GetContainerDirectoryResult] = {

    logger.info(s"Reading container directory [$ctName:$dirName]")
    implicit val eCtxt = testKit.system.dispatcher

    ctProxy.ask(ConfiguredContainer_?(ctName)).mapTo[ConfiguredContainer].flatMap { cc =>
      cc.cut match {
        case None => throw new Exception(s"Container with name [$ctName] not found.")
        case Some(cut) => ctProxy.ask(GetContainerDirectory(cut, dirName)).mapTo[GetContainerDirectoryResult]
      }
    }
  }

  def saveContainerDirectory(baseDir: String, dir: ContainerDirectory): Unit = {
    dir.content.foreach {
      case (name, content) =>
        val file = new File(s"$baseDir/$name")
        file.getParentFile().mkdirs()

        if (content.size > 0) {
          val fos = new FileOutputStream(file)
          fos.write(content)
          fos.flush()
          fos.close()
        }
    }
  }

  def execContainerCommand(
    ctProxy: ActorRef, ctName: String, cmdTimeout: FiniteDuration, user: String, cmd: String*
  )(implicit timeout: Timeout, testKit: TestKit): Future[ExecuteContainerCommandResult] = {

    implicit val eCtxt = testKit.system.dispatcher

    ctProxy.ask(ConfiguredContainer_?(ctName)).mapTo[ConfiguredContainer].flatMap { cc =>
      cc.cut match {
        case None => Future(ExecuteContainerCommandResult(Left(new Exception(s"Container with name [$ctName] not found."))))
        case Some(cut) =>
          ctProxy.ask(ExecuteContainerCommand(cut, cmdTimeout, user, cmd: _*))(cmdTimeout).mapTo[ExecuteContainerCommandResult]
      }
    }
  }

  def assertCondition(condition: Condition)(implicit testKit: TestKit): Boolean = {

    implicit val eCtxt = testKit.system.dispatcher

    val checker = testKit.system.actorOf(ConditionActor.props(condition))

    val checkFuture = (checker ? CheckCondition)(condition.timeout).map {
      case cr: ConditionCheckResult => cr.allSatisfied
      case _ => false
    }

    Await.result(checkFuture, condition.timeout)
  }
}
