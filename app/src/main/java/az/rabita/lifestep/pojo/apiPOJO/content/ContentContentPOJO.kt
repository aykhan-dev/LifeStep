package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class ContentContentPOJO(
    @Json(name = "Id") val id: Long,
    @Json(name = "Key") val transKey: String,
    @Json(name = "Value") val text: String,
    @Json(name = "ImageUrl") val url: String?
)