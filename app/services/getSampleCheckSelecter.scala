package services

import akka.actor.ActorSystem
import com.pharbers.driver.PhRedisDriver
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.pattern.frame.Brick
import mode.sampleCheckSelecter
import play.api.mvc.Request

case class getSampleCheckSelecter()(implicit val rq: Request[model.RootObject], implicit val actorSystem: ActorSystem)
        extends Brick with CirceJsonapiSupport{
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "sampleCheckSelecter"

    var sampleCheckSelecter: sampleCheckSelecter = _

    override def prepare: Unit = sampleCheckSelecter = formJsonapi[sampleCheckSelecter](rq.body)

    override def exec: Unit = {
        val rd = new PhRedisDriver()
        sampleCheckSelecter.mkt_list = rd.getSetAllValue(sampleCheckSelecter.job_id).map(x => {
            rd.getMapValue(x, "mkt")
        }).toList
        sampleCheckSelecter.ym_list = rd.getSetAllValue(sampleCheckSelecter.job_id).map(x => {
            rd.getMapValue(x, "ym")
        }).toList
    }

    override def done: Option[String] = {
        None
    }

    override def goback: model.RootObject = toJsonapi(sampleCheckSelecter)
}
