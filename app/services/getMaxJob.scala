package services

import java.util.{Date, UUID}

import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.models.entity.max.phmaxjob
import com.pharbers.pattern.frame.Brick
import play.api.mvc.Request

case class getMaxJob()(implicit val rq: Request[model.RootObject])
        extends Brick with CirceJsonapiSupport{
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "get maxJOb"

    var pHMaxJob: phmaxjob = null

    override def prepare: Unit = {
        pHMaxJob = formJsonapi[phmaxjob](rq.body)
    }

    override def exec: Unit = {
        val id = UUID.randomUUID().toString
        val date = new Date().toString
        pHMaxJob.id = id
        pHMaxJob.job_id = id
        pHMaxJob.date = date
    }

    override def done: Option[String] = {
        None
    }

    override def goback: model.RootObject = toJsonapi(pHMaxJob)
}
