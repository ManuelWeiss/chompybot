package controllers

import play.api.mvc.{Action, Controller}
import play.api.Logger
import play.api.libs.json.Json

object Application extends Controller {
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
    "homepage": "https://example.com/myaddon",
    "self": "https://11f981ac.ngrok.com/capabilities"
  },
  "capabilities": {
    "hipchatApiConsumer": {
      "scopes": [
        "send_notification"
      ]
    },
    "installable": {
      "callbackUrl": "https://11f981ac.ngrok.com/installable"
	},
	"webhook": [{
		"url": "https://11f981ac.ngrok.com/message",
		"event": "room_message",
		"name": "messages"
	}]
  }
	}
	""")
  }
  
  // POST
  def installable = Action(parse.json) {
    request => {
      val json = request.body
      // log the received credentials:
      Logger.debug(Json.prettyPrint(json))
      Ok("success")
    }
  }

  // POST
  def message = Action(parse.json) {
    request => {
      val json = request.body
      Logger.debug(Json.prettyPrint(json))
      Ok("success")
    }
  }

//  def get = Action {
//    Ok(PersistentCounter.get.toString).withHeaders("X-HELLO-MOBIFY-ROBOT" -> "hi")
//  }



}