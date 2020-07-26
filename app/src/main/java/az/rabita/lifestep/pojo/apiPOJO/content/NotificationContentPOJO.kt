package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class NotificationContentPOJO(
    @Json(name = "Id") val id: String,
    @Json(name = "ImageUrl") val imageUrl: String,
    @Json(name = "Messages") val messages: String,
    @Json(name = "Title") val title: String,
    @Json(name = "Url") val url: String,
    @Json(name = "UsersId") val usersId: String,
    @Json(name = "UsersNotificationsTypesId") val usersNotificationsTypesId: Int
)