package com.reactivebuzz

import akka.actor._
import com.reactivebuzz.TwitterConnector.Request
import scala.collection.mutable
import java.util.UUID
import org.json4s._
import com.reactivebuzz.TwitterConnector.Response
import com.reactivebuzz.TwitterConnector.Request
import com.reactivebuzz.TweetsFetcher.{ProjectWithTweets, ProjectTweet}
import com.reactivebuzz.TwitterConnector.RequestException
import com.reactivebuzz.GitHubGetter.ProjectItem

object TweetsFetcher {
   case class ProjectTweet(author: String, text: String)
   case class ProjectWithTweets(project: ProjectItem, tweets: List[ProjectTweet])
   def props(connector:ActorRef) = Props(classOf[TweetsFetcher], connector)
}
class TweetsFetcher(connector:ActorRef) extends Actor with ActorLogging{

   def searchUrl(query:String) = s"https://api.twitter.com/1.1/search/tweets.json?lang=en&q=$query"

   var requests = mutable.Map[String, (ActorRef, ProjectItem)]()

   def receive = {
      case p : ProjectItem =>
         val id = UUID.randomUUID().toString
         requests += (id -> (sender(), p))
         log.info(s"Sending request to Twitter for project: ${p.name}")
         connector ! Request(id, searchUrl(p.name))
      case Response(id, json) =>
         implicit val formats = org.json4s.DefaultFormats
         val tweets = (json \ "statuses").extract[List[JValue]]
           .map(tweet =>
            ProjectTweet(
               (tweet \ "user" \ "name").extract[String],
               (tweet \ "text").extract[String]
            ))
         val (requester, project) = requests(id)
         log.info(s"Got ${tweets.size} tweets for project: ${project.name}")
         requester ! ProjectWithTweets(project, tweets)
         requests.remove(id)

      case Status.Failure(RequestException(id, ex)) =>
         log.error("Request failed", ex)
         requests.remove(id)

   }
}
