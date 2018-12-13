package services

import akka.actor.ActorSystem
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.models.entity.max.phmaxjob
import com.pharbers.pattern.frame.Brick
import com.pharbers.pattern2.detail.PhMaxJob
import play.api.mvc.Request
import com.pharbers.xmpp.xmppClient
import xmpp.callMaxXmppConsumer


case class sendMessage ()(implicit val rq: Request[model.RootObject], implicit val actorSystem: ActorSystem)
        extends Brick with CirceJsonapiSupport{
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    override val brick_name: String = "send message to max"

    var maxJob: phmaxjob = _

    override def prepare: Unit =  maxJob = formJsonapi[phmaxjob](rq.body)

    override def exec: Unit = {
        xmppClient.startLocalClient(actorSystem, new callMaxXmppConsumer(actorSystem))
        xmppClient.a ! maxJobToPhMaxJob(maxJob)
    }

    override def done: Option[String] = {
        None
    }

    override def goback: model.RootObject = toJsonapi(maxJob)

    private def maxJobToPhMaxJob(maxJob: phmaxjob): PhMaxJob ={
        val phMaxJob = new PhMaxJob()
        phMaxJob.call = maxJob.call
        phMaxJob.company_id = maxJob.company_id
        phMaxJob.date = maxJob.date
        phMaxJob.job_id = maxJob.job_id
        phMaxJob.message = maxJob.message
        phMaxJob.user_id = maxJob.user_id
        phMaxJob.yms = maxJob.yms
        phMaxJob
    }
}