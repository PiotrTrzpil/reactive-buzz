package com.reactivebuzz

import akka.actor._
import com.reactivebuzz.GitHubGetter.{ProjectsError, GetProjects, Projects}
import com.reactivebuzz.TwitterConnector.{TwitterAuthorized, Auth}
import com.reactivebuzz.TweetsFetcher.{ProjectWithTweets}
import com.reactivebuzz.GitHubGetter.ProjectsError
import com.reactivebuzz.GitHubGetter.GetProjects
import com.reactivebuzz.TwitterConnector.Auth
import com.reactivebuzz.GitHubGetter.Projects
import com.reactivebuzz.TwitterConnector.TwitterAuthorized
import scala.Some
import com.reactivebuzz.TweetsFetcher.ProjectWithTweets
import akka.actor.SupervisorStrategy.Escalate
import scala.concurrent.duration._


object ControllerActor {
   val props = Props[ControllerActor]
   case object Initialize
   case class PingMessage(text: String)
   case object GetTweets
   case class GitHubException(cause:Throwable) extends Exception()
}
class ControllerActor extends Actor with ActorLogging {
  import ControllerActor._
  
  val connector = context.actorOf(TwitterConnector.props, "TwitterConnector")
  val tweetsFetcher = context.actorOf(TweetsFetcher.props(self, connector), "TweetsFetcher")
  val projectWriter = context.actorOf(ProjectWriter.props, "ProjectWriter")
  val github = context.actorOf(GitHubGetter.props, "GitHubGetter")

   override val supervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
         case _: Throwable                => Escalate
      }

  def receive = inactive

  def inactive : Receive = {
  	case Initialize => 
	   log.info("Starting Reactive fetching")
      connector ! Auth()
      github ! GetProjects("reactive")
      context.become(initializing)
  }

   def checkReady(): Unit = {
      (githubProjects, twitterAuthorized) match {
         case (Some(Projects(_)), true) =>
            context.become(gettingTweets)
            self ! GetTweets
         case _ =>
      }
   }

   var githubProjects : Option[Projects] = None
   var twitterAuthorized = false

   def initializing : Receive = {
      case projects @ Projects(json) =>
         githubProjects = Some(projects)
         checkReady()
      case ProjectsError(e) =>
         throw GitHubException(e)
      case TwitterAuthorized() =>
         twitterAuthorized = true
         checkReady()

   }
   def gettingTweets : Receive = {
      case GetTweets =>
         githubProjects.get.projects.foreach(project => {
            tweetsFetcher ! project
         })
      case projWithTweets : ProjectWithTweets =>
         projectWriter ! projWithTweets
   }
}

