package com.reactivebuzz

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import com.reactivebuzz.GitHubGetter.{ProjectItem, ProjectsError, Projects, GetProjects}
import org.json4s._
import org.json4s.native.JsonMethods._
import dispatch._, Defaults._
import org.json4s.JValue

object GitHubGetter {
   def props = Props(classOf[GitHubGetter])
   case class ProjectItem(name:String, description: String, owner: String, language: String, url: String)
   case class GetProjects(query:String)
   case class Projects(projects:List[ProjectItem])
   case class ProjectsError(exc:Throwable)
}
class GitHubGetter() extends Actor with ActorLogging {

   val numberOfProjects = 10
   def queryUrl(query:String) = s"https://api.github.com/search/repositories?q=$query&page=1&per_page=$numberOfProjects"

   def receive = {
      case GetProjects(query) =>
         val asker = sender()
         val svc = url(queryUrl(query))
         log.info(s"Getting github projects by query: $query")
         Http(svc OK as.String).map(jsonString => {
            val json = parse(jsonString)
            implicit val formats =  org.json4s.DefaultFormats
            val items = (json \ "items").extract[List[JValue]].take(numberOfProjects)
            log.info(s"Got ${items.size}} github projects.")
            val projects = items.map(item => {
               ProjectItem(
                  (item \ "name").extract[String],
                  (item \ "description").extract[String],
                  (item \ "owner" \ "login").extract[String],
                  (item \ "language").extract[String],
                  (item \ "html_url").extract[String])
            })

            asker ! Projects(projects)
         }).recover{case e => asker ! ProjectsError(e)}

   }
}
