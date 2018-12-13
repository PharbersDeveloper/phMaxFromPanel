package mode

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

object checkBodyEncoders {
    implicit val sampleBodyEncoders = Encoder.instance[sampleCheckBody](x => {
        Json.fromFields(
            List(("data",Json.fromFields(
                List(("id", x.id.asJson),("type", x.`type`.asJson),("attributes",getSampleBodyAttributes(x)))
            )))
        )
    })

    implicit val resultCheckEncoders = Encoder.instance[resultCheck](x => {
        Json.fromFields(List(("data",Json.fromFields(
            List(("id", x.id.asJson),("type", x.`type`.asJson),("attributes",getSampleBodyAttributes(x)))
        )))
        )
    })

    def getSampleBodyAttributes(sampleCheckBody: sampleCheckBody): Json ={
        Json.fromFields(
            List(("ym", sampleCheckBody.ym.asJson)
                ,("user_id", sampleCheckBody.user_id.asJson)
                ,("sales", sampleCheckBody.sales.asJson)
                ,("product", sampleCheckBody.product.asJson)
                ,("notfindhospital", sampleCheckBody.notfindhospital.asJson)
                ,("market", sampleCheckBody.market.asJson)
                ,("job_id", sampleCheckBody.job_id.asJson)
                ,("hospital", sampleCheckBody.hospital.asJson)
                ,("company_id", sampleCheckBody.company_id.asJson)
            )
        )
    }

    def getResultCheckAttributes(resultCheck: resultCheck): Json ={
        Json.fromFields(
            List(("ym", resultCheck.ym.asJson)
                ,("user_id", resultCheck.user_id.asJson)
                ,("region", resultCheck.region.asJson)
                ,("trend", resultCheck.trend.asJson)
                ,("indicators", resultCheck.indicators.asJson)
                ,("market", resultCheck.market.asJson)
                ,("job_id", resultCheck.job_id.asJson)
                ,("mirror", resultCheck.mirror.asJson)
                ,("company_id", resultCheck.company_id.asJson)
            )
        )
    }
}

