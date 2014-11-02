package com.reactivebuzz

import akka.actor.ActorSystem
import scala.io.StdIn
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")
  val pingActor = system.actorOf(ControllerActor.props, "ControllerActor")
   pingActor ! ControllerActor.Initialize

  system.awaitTermination()
}