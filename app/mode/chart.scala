package mode

import com.pharbers.jsonapi.json.FieldNames
import io.circe.Encoder
import play.api.libs.json.Json

//class chart {
//    implicit val chartJsonFormat = Json.format[chart]
//    var baselines: List[String] = Nil
//    var currentNumber = 0.0
//    var lastYearNumber = ""
//    var samplenumbers: List[String] = Nil
//}
case class chart(var baselines: List[String] = Nil, var currentNumber: Double = 0.0, var lastYearNumber: String = "", var samplenumbers: List[String] = Nil)
