package mode

import com.pharbers.macros.api.commonEntity

class sampleCheckBody extends commonEntity {
    var company_id = ""
    var job_id = ""
    var market = ""
    var user_id = ""
    var ym = ""
    var notfindhospital: List[hospital] = Nil
    var hospital: chart = _
    var product: chart = _
    var sales: chart = _
}
