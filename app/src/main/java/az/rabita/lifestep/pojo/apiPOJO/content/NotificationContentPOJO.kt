package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class NotificationContentPOJO(
    @Json(name = "Id") val id: Long,
    @Json(name = "ImageUrl") val imageUrl: String,
    @Json(name = "Messages") val messages: String,
    @Json(name = "Title") val title: String
)