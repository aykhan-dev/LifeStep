package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class AssocationDetailsContentPOJO(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String,
    @Json(name = "NeedSteps") val stepNeed: Long,
    @Json(name = "Balance") val balance: Long,
    @Json(name = "DonationPercent") val percent: Int,
    @Json(name = "ImageUrl") val url: String,
    @Json(name = "Description") val description: String,
    @Json(name = "CreatedDate") val createdDate: String,
    @Json(name = "ExpireDate") val expireDate: String
)