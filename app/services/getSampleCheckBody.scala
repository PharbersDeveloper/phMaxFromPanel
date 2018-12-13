package services

import java.util.Base64

import akka.actor.ActorSystem
import com.pharbers.driver.PhRedisDriver
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import com.pharbers.macros.convert.mongodb.TraitRequest
import com.pharbers.models.request.request
import com.pharbers.models.{request => requestObj}
import com.pharbers.mongodb.dbtrait.DBTrait
import com.pharbers.pattern.module.DBManagerModule
import com.pharbers.sercuity.Sercurity
import mode.{baseLine, chart, hospital, sampleCheckBody}
import play.api.mvc.Request

case class getSampleCheckBody()(implicit val rq: Request[model.RootObject],
                                implicit val actorSystem: ActorSystem,
                                implicit val dbt: DBManagerModule)
        extends CirceJsonapiSupport{
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    val brick_name: String = "getSampleCheckBody"

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("market").get.asInstanceOf[DBTrait[TraitRequest]]

    var sampleCheckBody: sampleCheckBody = _

    def prepare: Unit = sampleCheckBody = formJsonapi[sampleCheckBody](rq.body)

    def exec: Unit = {
        val request = new request()
        val ym = sampleCheckBody.ym
        val company = sampleCheckBody.company_id
        val mkt = sampleCheckBody.market
        val user = sampleCheckBody.user_id
        request.res = "baseLine"
        request.fmcond = Some(requestObj.fm2c(0, 1000))
        request.eqcond = Some(List(requestObj.eq2c("Company", company),
            requestObj.eq2c("Market", mkt)))
        request.gtecond = Some(List(requestObj.gte2c("Date", (ym.substring(0, 4).toInt - 1).toString + "01")))
        request.ltecond = Some(List(requestObj.lte2c("Date", (ym.substring(0, 4).toInt - 1).toString + "12")))
        val hospitalData: chart = chart()
        val productData: chart = chart()
        val salesData: chart = chart()
        val baseLineLst = queryMultipleObject[baseLine](request)
        baseLineLst.foreach(x => {
            hospitalData.baselines = hospitalData.baselines :+ x.HOSP_ID
            productData.baselines = productData.baselines :+ x.Prod_Name
            salesData.baselines = productData.baselines :+ x.Sales
        })
        val lastYearLine = baseLineLst.find(x => x.Date.equals((ym.substring(0, 4).toInt - 1)
                .toString + ym.substring(4))).getOrElse(new baseLine)
        hospitalData.lastYearNumber = lastYearLine.HOSP_ID
        productData.lastYearNumber = lastYearLine.Prod_Name
        salesData.lastYearNumber = lastYearLine.Sales
        val sampleNum: Double => List[String] = x => {
            val re = new Array[String](12).map(_ => "0")
            re(ym.substring(4).toInt - 1) = x.toString
            re.toList
        }
        val rd = new PhRedisDriver()
        val singleJobKey = Base64.getEncoder.encodeToString((company + "#" + ym + "#" + mkt).getBytes())
        val notPanelHospKey = Sercurity.md5Hash(user + company + ym + mkt + "not_panel_hosp_lst")
        hospitalData.currentNumber = rd.getMapValue(singleJobKey, "panel_hosp_count").toDouble
        hospitalData.samplenumbers = sampleNum(hospitalData.currentNumber)
        productData.currentNumber = rd.getMapValue(singleJobKey, "panel_prod_count").toDouble
        productData.samplenumbers = sampleNum(productData.currentNumber)
        salesData.currentNumber = rd.getMapValue(singleJobKey, "panel_sales").toDouble
        salesData.samplenumbers = sampleNum(salesData.currentNumber)

        sampleCheckBody.notfindhospital = rd.getSetAllValue(notPanelHospKey).toList.zipWithIndex.map(x => hospital(x._1, x._2))

        sampleCheckBody.hospital = hospitalData
        sampleCheckBody.product = productData
        sampleCheckBody.sales = salesData
    }

    def goback: sampleCheckBody = {
        prepare
        exec
        sampleCheckBody
    }
}
