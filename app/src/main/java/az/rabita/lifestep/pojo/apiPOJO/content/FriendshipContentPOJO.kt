package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class FriendshipContentPOJO(
    @Json(name = "FriendCount") val friendsCount: Int,
    @Json(name = "PendingCount") val requestCount: Int
)