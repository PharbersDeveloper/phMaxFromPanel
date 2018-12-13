package mode

import com.pharbers.macros.api.commonEntity

class resultCheck extends commonEntity {
    var company_id = ""
    var indicators: Map[String, String] = _
    var job_id = ""
    var market = ""
    var mirror: Map[String, mirror] = _
    var region: List[regionData] = Nil
    var trend: List[trendData] = Nil
    var user_id = ""
    var ym = ""
}

case class mirror(current: List[mirrorData] = Nil, lastyear: List[mirrorData] = Nil)

case class mirrorData(area: String, marketSales: String, percentage: Double, productSales: String)

case class regionData(name: String, percentage: Double, productSales: String, value: String)

case class trendData(date: String, market: String, percentage: Double)