package com.reactivebuzz

import akka.actor.{Props, Actor}
import com.reactivebuzz.TweetsFetcher.ProjectWithTweets
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write
object ProjectWriter {
   def props = Props(classOf[ProjectWriter])
}
class ProjectWriter() extends Actor{
   override def receive = {
      case proj @ ProjectWithTweets(project, tweets) =>
         implicit val formats = Serialization.formats(NoTypeHints)
         val ser = write(proj)
         println(ser)

   }
}
