package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class RankerContentPOJO(
    @Json(name = "Id") val id: String,
    @Json(name = "FullName") val fullName: String,
    @Json(name = "Count") val count: Long,
    @Json(name = "ImageUrl") val url: String
)