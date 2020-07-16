package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class HistoryItemContentPOJO(
    @Json(name = "Fullname") val fullName: String,
    @Json(name = "CreatedDate") val date: String,
    @Json(name = "Count") val count: Long
)