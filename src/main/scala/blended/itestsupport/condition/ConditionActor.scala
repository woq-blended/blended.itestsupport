package blended.itestsupport.condition

import akka.actor.Props
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}

object ConditionActor {
  def props(cond: Condition): Props = cond match {
    case pc: ParallelComposedCondition => ParallelConditionActor.props(pc)
    case sc: SequentialComposedCondition => SequentialConditionActor.props(sc)
    case _ => Props(new ConditionActor(cond))
  }

  /**
   * Use this object to query an actor that encapsulates a condition.
   */
  case object CheckCondition

  /**
   * This message collects the results of nested Conditions
   */
  case class ConditionCheckResult(satisfied: List[Condition], timedOut: List[Condition]) {
    def allSatisfied: Boolean = timedOut.isEmpty

    def reportTimeouts: String =
      timedOut.mkString(
        s"\nA total of [${timedOut.size}] conditions have timed out", "\n", ""
      )

    override def toString(): String = s"${getClass().getSimpleName()}(satisfied=${satisfied},timedOut=${timedOut})}"
  }

  object ConditionCheckResult {
    def apply(results: List[ConditionCheckResult]) = {
      new ConditionCheckResult(
        results.map { r => r.satisfied }.flatten,
        results.map { r => r.timedOut }.flatten
      )
    }
  }

}

class ConditionActor(cond: Condition) extends Actor with ActorLogging {
  import ConditionActor._

  case object Tick

  case object Check

  implicit val ctxt = context.system.dispatcher

  def receive = initializing

  def initializing: Receive = {
    case CheckCondition =>
      val requestor = sender()
      log.debug(s"Checking condition [${cond.description}] with timeout [${cond.timeout}] on behalf of [${requestor}]")
      val timer = context.system.scheduler.scheduleOnce(cond.timeout, self, Tick)
      context.become(checking(requestor, timer))
      self ! Check
  }

  def checking(checkingFor: ActorRef, timer: Cancellable): Receive = {
    case CheckCondition =>
      log.warning(
        s"""
           |
           |You have sent another CheckCondition message from [${sender}],
           |but this actor is already checking on behalf of [${checkingFor}].
           |
         """.stripMargin
      )
    case Check => cond.satisfied match {
      case true =>
        log.info(s"Condition [${cond}] is now satisfied.")
        timer.cancel()
        val response = ConditionCheckResult(List(cond), List.empty)
        log.debug(s"Answering [${response}] to [${checkingFor}]")
        checkingFor ! response
        context.stop(self)
      case false =>
        context.system.scheduler.scheduleOnce(cond.interval, self, Check)
    }
    case Tick =>
      log.info(s"Condition [${cond}] hast timed out.")
      log.debug(s"Answering to [${checkingFor}]")
      checkingFor ! ConditionCheckResult(List.empty, List(cond))
      context.stop(self)
  }
}
