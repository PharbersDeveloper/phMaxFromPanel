package controllers

import java.io.File

import play.api.mvc._
import io.circe.syntax._
import akka.actor.ActorSystem
import play.api.libs.circe.Circe
import javax.inject.{Inject, Singleton}
import com.pharbers.pattern.frame.PlayEntry
import com.pharbers.jsonapi.model.RootObject
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.pattern.module.{DBManagerModule, RedisManagerModule}
import play.api.libs.json.Json
import play.core.Paths
import services._
import mode.checkBodyEncoders._

@Singleton
class Controller @Inject()(implicit val cc: ControllerComponents,
                           implicit val dbt: DBManagerModule,
                           implicit val sys: ActorSystem)
        extends AbstractController(cc) with Circe with CirceJsonapiSupport {

    lazy val configDir : String = System.getProperty("user.dir")

    def routes(pkg: String, step: String): Action[RootObject] = Action(circe.json[RootObject]) { implicit request =>
        Ok(
            (pkg, step) match {
                case ("maxjobpush", "0") => PlayEntry().excution(pushPanel()).asJson
                case ("maxjobgenerate", "0") => PlayEntry().excution(getMaxJob()).asJson
                case ("maxjobsent", "0") => PlayEntry().excution(sendMessage()).asJson
                case ("samplecheckselecter", "0") => PlayEntry().excution(getSampleCheckSelecter()).asJson
                case ("samplecheckbody", "0") => getSampleCheckBody().goback.asJson
                case ("resultcheck", "0") => getResultCheck().goback.asJson
            }
        )
    }

    def upload() = Action(parse.multipartFormData) { request =>
        val file = request.body.files.map(x => {
            try {
                val filename = x.filename
                val filePath = s"$configDir/resource/xlsx/$filename"
                x.ref.moveTo(new File(filePath), true)
                filename
            }catch {
                case _: Exception => BadRequest("Bad Request for input")
            }
        }).mkString(",")
        Ok(Json.obj(
            "error" -> "",
            "result" -> Json.obj("file" -> file),
            "status" -> "ok"
        ))
    }
}
