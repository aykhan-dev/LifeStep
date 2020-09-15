package az.rabita.lifestep.pojo.apiPOJO.content

import az.rabita.lifestep.pojo.apiPOJO.status.StatusPOJO
import com.squareup.moshi.Json

data class AllInOneOwnProfileInfoServerResponsePOJO(
    @Json(name = "Status") val status: StatusPOJO,
    @Json(name = "userInfo") val userInfo: List<OwnProfileInfoContentPOJO>,
    @Json(name = "ReportWeekly") val dailyStats: List<DailyContentPOJO>,
    @Json(name = "ReportMonthly") val monthlyStats: List<MonthlyContentPOJO>
)