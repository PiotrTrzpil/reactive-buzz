package com.reactivebuzz

import akka.actor._
import java.net.URLEncoder
import java.util.Base64
import dispatch.{url, Http, as}
import com.reactivebuzz.GitHubGetter.Projects
import org.json4s._
import org.json4s.native.JsonMethods._
import com.reactivebuzz.TwitterConnector._
import scala.util.Success
import akka.pattern._
import com.reactivebuzz.TwitterConnector.Response
import scala.util.Failure
import scala.Some
import com.reactivebuzz.TwitterConnector.RequestException
import com.reactivebuzz.TwitterConnector.Auth
import scala.util.Success
import com.reactivebuzz.TwitterConnector.Request

object TwitterConnector {
   val props = Props[TwitterConnector]
   case class Auth()
   case class TwitterAuthorized()
   case class Response(id:String, value:JValue)
   case class Request(id:String, url:String)
   case class RequestException(id:String, cause:Throwable) extends Exception(cause)
}

class TwitterConnector extends Actor with ActorLogging{

   val authUrl = "https://api.twitter.com/oauth2/token"

   var authToken : Option[String] = None
   implicit val exec = context.system.dispatcher

   def receive = {

      case Auth() =>
         log.info(s"Authenticating in Twitter.")
         val asker = sender()
         auth().onComplete {
         case Success(token) =>
            log.info(s"Got token from Twitter.")
            authToken = Some(token)
            asker ! TwitterAuthorized()
         case Failure(ex) => throw ex
      }

      case Request(id, url) => authToken match {
         case Some(token) => request(url, token)
           .map(Response(id,_))
           .recover{case e => RequestException(id, e)}
           .pipeTo(sender())
         case None => sender() ! Status.Failure(RequestException(id, new Exception("Not authenticated.")))
      }
   }

   def request(urlString : String, token: String) = {

      val svc = url(urlString).GET
        .addHeader("Authorization", s"Bearer $token")
      Http(svc > as.String)
        .map(parse(_))
   }

   def auth() = {
      val charset = "UTF-8"
      val key = context.system.settings.config.getString("twitter.key")
      val secret = context.system.settings.config.getString("twitter.secret")
      val authString = URLEncoder.encode(key, charset) + ':' + URLEncoder.encode(secret, charset)
      val encoded = Base64.getEncoder.encodeToString(authString.getBytes(charset))
      val svc = url(authUrl).POST
        .addHeader("Authorization", s"Basic $encoded")
        .setContentType("application/x-www-form-urlencoded", "UTF-8")
        .setBody("grant_type=client_credentials")
      implicit val exec = context.system.dispatcher
      val future = Http(svc > as.String)
      future
        .map(parse(_))
        .map(json => (json \ "token_type", json \ "access_token"))
        .collect{ case (JString("bearer"), JString(token)) => token }


   }

}
