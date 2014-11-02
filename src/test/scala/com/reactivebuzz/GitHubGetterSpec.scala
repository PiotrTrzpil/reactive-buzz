package com.reactivebuzz

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, ImplicitSender }
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import com.reactivebuzz.GitHubGetter.{ProjectsError, Projects, GetProjects}

class GitHubGetterSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("MySpec"))
 
  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }
 
  "A Ping actor" must {
    "send back a ping on a pong" in {
      val githubActor = system.actorOf(GitHubGetter.props)
       githubActor ! GetProjects("Reactive")
      expectMsgClass(classOf[ProjectsError])
    }
  }



}
