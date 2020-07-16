package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class DailyContentPOJO(
    @Json(name = "CreatedDate") val createdDate: String,
    @Json(name = "ShortName") val shortName: String,
    @Json(name = "Count") val count: Long
)