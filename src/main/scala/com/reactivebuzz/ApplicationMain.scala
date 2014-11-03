package com.reactivebuzz

import akka.actor.{Props, Actor, ActorSystem}
import scala.io.StdIn
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")

   val supervisor = system.actorOf(Props[SupervisorActor], "Supervisor")
   system.awaitTermination()
}