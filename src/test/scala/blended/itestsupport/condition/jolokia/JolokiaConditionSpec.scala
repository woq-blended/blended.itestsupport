package blended.itestsupport.condition.jolokia

import akka.testkit.{TestActorRef, TestProbe}
import blended.itestsupport.condition.{Condition, ConditionActor}
import blended.itestsupport.jolokia.JolokiaAvailableCondition
import blended.testsupport.TestActorSys
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration._

import blended.itestsupport.condition.ConditionActor.CheckCondition
import blended.itestsupport.condition.ConditionActor.ConditionCheckResult

class JolokiaConditionSpec extends WordSpec
  with Matchers {

  "The JolokiaAvailableCondition" should {

    "be satisfied with the intra JVM Jolokia" in TestActorSys { testkit =>
      implicit val system = testkit.system
      val probe = TestProbe()

      val t = 10.seconds

      val condition = JolokiaAvailableCondition("http://localhost:7777/jolokia", Some(t))

      val checker = TestActorRef(ConditionActor.props(cond = condition))
      checker.tell(CheckCondition, probe.ref)

      probe.expectMsg(t, ConditionCheckResult(List(condition), List.empty[Condition]))
    }

    "fail with a not existing Jolokia" in TestActorSys { testkit =>
      implicit val system = testkit.system
      val probe = TestProbe()

      val t = 10.seconds

      val condition = JolokiaAvailableCondition("http://localhost:8888/jolokia", Some(t))

      val checker = TestActorRef(ConditionActor.props(cond = condition))
      checker.tell(CheckCondition, probe.ref)
      probe.expectMsg(t + 1.second, ConditionCheckResult(List.empty[Condition], List(condition)))
    }
  }
}
