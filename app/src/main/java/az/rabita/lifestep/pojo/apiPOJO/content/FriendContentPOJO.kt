package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class FriendContentPOJO(
    @Json(name = "Id") val id: String,
    @Json(name = "Fullname") val fullName: String,
    @Json(name = "Count") val balance: Int,
    @Json(name = "ImageUrl") val imageUrl: String
)