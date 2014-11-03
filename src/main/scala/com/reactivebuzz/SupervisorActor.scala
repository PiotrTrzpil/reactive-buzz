package com.reactivebuzz

import akka.actor.{PoisonPill, OneForOneStrategy, Actor}
import akka.actor.SupervisorStrategy.{Stop, Escalate}
import scala.concurrent.duration._
class SupervisorActor extends Actor{

   override val supervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
         case _: Throwable                =>
            context.system.shutdown()
            Stop
      }

   val controllerActor = context.actorOf(ControllerActor.props, "ControllerActor")
   controllerActor ! ControllerActor.Initialize


   def receive = {
      case _ =>
   }
}
