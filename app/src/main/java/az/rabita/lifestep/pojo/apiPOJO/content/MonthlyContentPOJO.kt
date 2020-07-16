package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class MonthlyContentPOJO(
    @Json(name = "Months") val month: Int,
    @Json(name = "ShortName") val monthName: String,
    @Json(name = "Count") val count: Long
)