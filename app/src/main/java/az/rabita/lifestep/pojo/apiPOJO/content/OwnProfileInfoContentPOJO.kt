package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class OwnProfileInfoContentPOJO(
    @Json(name = "Id") val id: String,
    @Json(name = "Fullname") val fullName: String,
    @Json(name = "Surname") val surname: String,
    @Json(name = "Name") val name: String,
    @Json(name = "Email") val email: String,
    @Json(name = "PhoneNumber") val phone: String,
    @Json(name = "ImageUrl") val url: String,
    @Json(name = "FriendsCount") val friendsCount: Int,
    @Json(name = "Balance") val balance: Long,
    @Json(name = "InvitationCode") val invitationCode: String,
    @Json(name = "CreatedDate") val createdDate: String
)