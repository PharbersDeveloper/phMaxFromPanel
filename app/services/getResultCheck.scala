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
import mode._
import play.api.mvc.Request

case class getResultCheck(implicit val rq: Request[model.RootObject],
                     implicit val actorSystem: ActorSystem,
                     implicit val dbt: DBManagerModule)
        extends CirceJsonapiSupport{
    import com.pharbers.macros._
    import com.pharbers.macros.convert.jsonapi.JsonapiMacro._

    implicit val db: DBTrait[TraitRequest] = dbt.queryDBInstance("aggregation").get.asInstanceOf[DBTrait[TraitRequest]]

    var resultCheck: resultCheck = _

    def prepare: Unit = resultCheck = formJsonapi[resultCheck](rq.body)

    def exec: Unit = {
        val company = resultCheck.company_id
        val ym = resultCheck.ym
        val mkt = resultCheck.market
        val lastYearYm = (ym.toInt - 100).toString
        val lastYearSingleJobKey: String = Base64.getEncoder.encodeToString((company +"#"+ lastYearYm +"#"+ mkt).getBytes())

        val request = new request
        request.fmcond = Some(requestObj.fm2c(0, 1000))
        request.res = lastYearSingleJobKey + "_CITY_SALES"
        val lastYmMaxCitySalesLst = queryMultipleObject[aggregation](request, "value")
        request.res = lastYearSingleJobKey + "_PROVINCE_SALES"
        val lastYmMaxProvSalesLst = queryMultipleObject[aggregation](request, "value")
        request.res = lastYearSingleJobKey + "_CITY_COMPANY_SALES"
        val lastYmCompanyCitySalesLst = queryMultipleObject[aggregation](request, "value")
        request.res = lastYearSingleJobKey + "_PROVINCE_COMPANY_SALES"
        val lastYmCompanyProvSalesLst = queryMultipleObject[aggregation](request, "value")
        val nationSalesLst = ((lastYearYm.toInt + 1) to (lastYearYm.substring(0, 4) + "12").toInt)
                .++ ((ym.substring(0, 4) + "01").toInt until ym.toInt)
                .map(x => {
                    val key = Base64.getEncoder.encodeToString((company +"#"+ x.toString +"#"+ mkt).getBytes())
                    request.res = key + "_NATION_SALES"
                    val maxSales = queryObject[aggregation](request).getOrElse(new aggregation).value
                    request.res = key + "_NATION_COMPANY_SALES"
                    val companySales = queryObject[aggregation](request).getOrElse(new aggregation).value
                    (x.toString, maxSales, companySales)
                })
                .toList

        val singleJobKey = Base64.getEncoder.encodeToString((company +"#"+ ym +"#"+ mkt).getBytes())
        val maxSalesCityLstKey = Sercurity.md5Hash(company + ym + mkt + "max_sales_city_lst_key")
        val maxSalesProvLstKey = Sercurity.md5Hash(company + ym + mkt + "max_sales_prov_lst_key")
        val companySalesCityLstKey = Sercurity.md5Hash(company + ym + mkt + "company_sales_city_lst_key")
        val companySalesProvLstKey = Sercurity.md5Hash(company + ym + mkt + "company_sales_prov_lst_key")
        val rd = new PhRedisDriver()
        val nationMaxSales = rd.getMapValue(singleJobKey, "max_sales")
        val nationCompanySales = rd.getMapValue(singleJobKey, "max_company_sales")
        val getSalesFromRedisValue: String => Array[String] = x => x.replaceAll("[\\[\\]]","").split(",")
        val maxCitySalesLst = rd.getListAllValue(maxSalesCityLstKey).map(getSalesFromRedisValue).sortBy(x => x(0))
        val maxProvSalesLst = rd.getListAllValue(maxSalesProvLstKey).map(getSalesFromRedisValue).sortBy(x => x(0))
        val companyCitySalesLst = rd.getListAllValue(companySalesCityLstKey).map(getSalesFromRedisValue).sortBy(x => x(0))
        val companyProvSalesLst = rd.getListAllValue(companySalesProvLstKey).map(getSalesFromRedisValue).sortBy(x => x(0))
        resultCheck.trend = nationSalesLst.map(x => {
            trendData(x._1, x._3.toString, x._3 / x._2)
        }) :+ trendData(ym, nationCompanySales, nationMaxSales.toDouble / nationCompanySales.toDouble)
        resultCheck.region = maxProvSalesLst.zip(companyProvSalesLst).map(x => {
            regionData(x._1(0), x._2(1).toDouble / x._1(1).toDouble, x._2(1), x._1(1))
        })
    }

    def goback: resultCheck = {
        prepare
        exec
        resultCheck
    }
}
