package models

import play.api.db._
import play.api.Play.current
import play.api.libs.json._

import anorm._
import anorm.SqlParser._

case class Consumer(
    oauthId: String,
    oauthSecret: String,
    capabilitiesUrl: String,
    roomId: Long,
    groupId: Long)

object Consumer {
    // (de-)serialize to/from Json
    implicit object ConsumerFormat extends Format[Consumer] {
        def reads(json: JsValue) = JsSuccess(
            Consumer(
                (json \ "oauthId").as[String],
                (json \ "oauthSecret").as[String],
                (json \ "capabilitiesUrl").as[String],
                (json \ "roomId").as[Long],
                (json \ "groupId").as[Long]))

        def writes(Consumer: Consumer) = JsObject(Seq(
            "oauthId" -> JsString(Consumer.oauthId),
            "oauthSecret" -> JsString(Consumer.oauthSecret),
            "capabilitiesUrl" -> JsString(Consumer.capabilitiesUrl),
            "roomId" -> JsNumber(Consumer.roomId),
            "groupId" -> JsNumber(Consumer.groupId)))
    }

    /**
    * Parse a Consumer from a SQL ResultSet
    */
    val parseConsumerRS = {
        str("Consumers.oauthId") ~
        str("Consumers.oauthSecret") ~
        str("Consumers.capabilitiesUrl") ~
        get[Long]("Consumers.roomId") ~
        get[Long]("Consumers.groupId") map {
            case oauthId ~ oauthSecret ~ capabilitiesUrl ~ roomId ~ groupId =>
                Consumer(oauthId, oauthSecret, capabilitiesUrl, roomId, groupId)
        }
    }

    /**
    * Retrieve a Consumer by roomId.
    *
    * @param id The roomId of the Consumer.
    */
    def findByRoomId(roomId: Long): Option[Consumer] = {
        DB.withConnection {
            implicit connection =>
                SQL("select * from Consumers where roomId = {roomId}").on('roomId -> roomId).as(parseConsumerRS.singleOpt)
        }
    }

    /**
    * Insert a Consumer.
    *
    * @param Consumer The Consumer object.
    */
    def insert(Consumer: Consumer) = {
        DB.withConnection {
            implicit connection =>
                SQL(
                    """
          insert into Consumers
          (oauthId, oauthSecret, capabilitiesUrl, roomId, groupId)
          values ( {oauthId}, {oauthSecret}, {capabilitiesUrl}, {roomId}, {groupId} )
          """).on(
                        'oauthId -> Consumer.oauthId,
                        'oauthSecret -> Consumer.oauthSecret,
                        'capabilitiesUrl -> Consumer.capabilitiesUrl,
                        'roomId -> Consumer.roomId,
                        'groupId -> Consumer.groupId).executeUpdate()
        }
    }
}
