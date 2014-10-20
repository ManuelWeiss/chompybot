package controllers

import play.api.mvc.{ Action, Controller }
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WS


object Application extends Controller {

    val URL = "https://dry-sierra-3468.herokuapp.com/"

    def index = Action {
        Ok(views.html.index("Hello Play Framework"))
    }

    def capabilities = Action {
        Ok("""
    {
  "name": "Chompybot",
  "description": "An add-on that makes suggestions for lunch",
  "key": "io.weiss.chompybot",
  "links": {
    "homepage": "https://dry-sierra-3468.herokuapp.com/",
    "self": "https://dry-sierra-3468.herokuapp.com/capabilities"
  },
  "capabilities": {
    "hipchatApiConsumer": {
      "scopes": [
        "send_notification"
      ]
    },
    "installable": {
      "callbackUrl": "https://dry-sierra-3468.herokuapp.com/installable"
    },
    "webhook": [{
        "url": "https://dry-sierra-3468.herokuapp.com/message",
        "event": "room_message",
        "name": "messages"
    }]
  }
    }
    """)
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
            {
                val json = request.body
                Logger.debug(Json.prettyPrint(json))
                Ok("success")
            }
    }

//    def send_message(message: String) = Action {
//        Async {
//            val messageURL = "http://"
//            WS.url(messageURL).get().map { response =>
//                Ok("Feed title: " + (response.json \ "title").as[String])
//            }
//        }
//    }

}
