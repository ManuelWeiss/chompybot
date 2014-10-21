package controllers

import play.api.mvc.{ Action, Controller }
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws._
import com.ning.http.client.Realm.AuthScheme
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.util.{ Success, Failure }
import play.api.cache.Cache
import play.api.Play.current
import com.github.nscala_time.time.Imports._

object Application extends Controller {

    val URL = "https://dry-sierra-3468.herokuapp.com/"
    val user = "e16e91ae-bf8e-4cc4-a52d-c08cdf497efc"
    val pass = "kzZImKPmbGeWyCadw7MD1VsBPSSAEG05NS5YH0om"
    val roomId = 546167

    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    def index = Action {
        Ok(views.html.index("Hello Play Framework"))
    }

    def capabilities = Action {
        val caps = Json.obj(
            "name" -> "Chompybot",
            "description" -> "An add-on that makes suggestions for lunch",
            "key" -> "io.weiss.chompybot",
            "links" -> Json.obj(
                "homepage" -> URL,
                "self" -> s"$URL/capabilities"),
            "capabilities" -> Json.obj(
                "hipchatApiConsumer" -> Json.obj(
                    "scopes" -> Json.arr(
                        "send_notification",
                        "send_message")),
                "installable" -> Json.obj(
                    "callbackUrl" -> s"$URL/installable"),
                "webhook" -> Json.arr(Json.obj(
                    "url" -> s"$URL/message",
                    "event" -> "room_message",
                    "name" -> "messages"))))
        Ok(caps)
    }

    // receives POST
    def installable = Action(parse.json) {
        request =>
            {
                val json = request.body
                // log the received credentials:
                Logger.debug(Json.prettyPrint(json))
                Ok("success")
            }
    }

    // receives POST
    def receive_message = Action(parse.json) {
        request =>
            (request.body \ "item" \ "message" \ "message").asOpt[String].map { message =>
                {
                    Logger.debug(s"got message: $message")
                    val response = send_message(s"""did you just say "$message"?""")
                    response.onComplete {
                        case Success(body) => Logger.debug(s"response to send request: $body")
                        case Failure(t) => Logger.debug("failure: " + t.getMessage)
                    }
                    Ok("success")
                }
            }.getOrElse {
                val error = "No 'message' in " + request.body
                Logger.debug(error)
                BadRequest(error)
            }
    }

    type DT = org.joda.time.DateTime
    case class Token(token: String, expiryDate: DT)

    def get_token(): Future[Option[String]] = {
        Cache.getAs[Token]("authToken") match {
            case Some(cachedToken) => {
                if (cachedToken.expiryDate < DateTime.now + 5.seconds)
                    refresh_cached_token
                else
                    Future.successful(Some(cachedToken.token))
            }
            case None => refresh_cached_token
        }
    }

    def refresh_cached_token: Future[Option[String]] = {
        val futureToken = fetch_token(user, pass)
        futureToken.onComplete {
            case Success(token) => Cache.set("authToken", token)
            case Failure(t) => Logger.debug(s"retrieving auth token failed: $t")
        }
        futureToken.map {
            case Some(t) => Some(t.token)
            case None => None
        }
    }

    def fetch_token(user: String, pass: String): Future[Option[Token]] = {
        val tokenURL = "https://api.hipchat.com/v2/oauth/token"
        WS.url(tokenURL)
            .withAuth(user, pass, AuthScheme.BASIC)
            .post(Map(
                "grant_type" -> Seq("client_credentials"),
                "scope" -> Seq("send_notification")))
            .map {
                case (response) =>
                    if (response.status == 200) {
                        val token = (response.json \ "access_token").as[String]
                        val expiresIn = (response.json \ "expires_in").as[Int]
                        val validUntil = DateTime.now + expiresIn
                        Some(Token(token, validUntil))
                    }
                    else {
                        Logger.debug("couldn't get auth token: " + response.body)
                        None
                    }
            }
    }

    def messagetest = Action {
        val r = send_message("This is a test")
        r.onComplete {
            case Success(body) => Logger.debug(s"response to send request: $body")
            case Failure(t) => Logger.debug("failure: " + t.getMessage)
        }
        Ok("success")
    }

    def send_message(message: String): Future[String] = {
        for {
            token <- get_token()
            response <- send_message_with_token(message, token.get) if (token.isDefined)
        } yield response
    }

    def send_message_with_token(message: String, token: String): Future[String] = {
        val messageURL = s"https://api.hipchat.com/v2/room/$roomId/notification"
        WS.url(messageURL)
            .withHeaders("content-type" -> "application/json")
            .withQueryString("auth_token" -> token)
            .post(Json.obj("message" -> message))
            .map {
                case (response) =>
                    if (response.status == 200)
                        "Success"
                    else
                        "send_message failed: " + response.body
            }
    }

    //{
    //  "oauthId" : "8534e74b-8b8d-4270-812d-73bff87e5d28",
    //  "capabilitiesUrl" : "https://api.hipchat.com/v2/capabilities",
    //  "roomId" : 546167,
    //  "groupId" : 35222,
    //  "oauthSecret" : "IAuNDb5Tu2tp9BYmA12hQ53wdqtRCim4mnFQx7IJ"
    //}

    // get token:
    //  curl  -X POST -d "grant_type=client_credentials&scope=send_notification" -u "8534e74b-8b8d-4270-812d-73bff87e5d28:IAuNDb5Tu2tp9BYmA12hQ53wdqtRCim4mnFQx7IJ" https://api.hipchat.com/v2/oauth/token
    // {"access_token": "F7hTDT3sqQPuGKP4HSOIRadv4Fko0qNVAhgARFFj", "expires_in": 3599, "group_id": 35222, "group_name": "Moonfruit", "scope": "send_notification", "token_type": "bearer"}

    // send message:
    //  curl -X POST -H "content-type: application/json" -d '{"message":"Hello Hipchat!"}' https://api.hipchat.com/v2/room/546167/notification?auth_token=XioEg4li1eScCtPMKklSoGZhReHgxhjuctJJNvp8
}
