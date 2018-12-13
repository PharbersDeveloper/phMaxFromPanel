package xmpp

import akka.actor.ActorSystem
import com.pharbers.macros._
import com.pharbers.jsonapi.model._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern2.detail.{PhMaxJob, commonresult}
import io.circe.syntax._
import com.pharbers.xmpp.xmppTrait

class callMaxXmppConsumer(context : ActorSystem) extends xmppTrait with CirceJsonapiSupport {

    override val encodeHandler: commonresult => String = obj =>
        toJsonapi(obj).asJson.noSpaces

    override val decodeHandler: String => commonresult =str =>
        formJsonapi[PhMaxJob](decodeJson[RootObject](parseJson(str)))

    override val consumeHandler: String => Unit = x => {
        println(x)
    }
}
