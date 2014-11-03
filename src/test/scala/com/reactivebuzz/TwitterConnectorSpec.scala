package com.reactivebuzz

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import com.reactivebuzz.GitHubGetter.{GetProjects, ProjectsError}
import scala.concurrent.Await
import scala.concurrent.duration._
import org.json4s.JValue
import org.json4s.JsonAST.{JValue, JArray}
import org.json4s.JValue

class TwitterConnectorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "A TwitterConnector actor" must {
    "authenticate with Twitter and get some tweets" in {
      implicit val formats = org.json4s.DefaultFormats
      val actor = TestActorRef[TwitterConnector](TwitterConnector.props)
      val token: String = Await.result(actor.underlyingActor.auth(), 10 seconds)
      val result: JValue = Await.result(actor.underlyingActor.request("https://api.twitter.com/1.1/search/tweets.json?q=Akka&lang=en", token), 10 seconds)
      val tweets = (result \ "statuses").extract[List[JValue]]
         .map(a => (a \ "text").extract[String])
      tweets.size should be > 0
    }
  }



}
