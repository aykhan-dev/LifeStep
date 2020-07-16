package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class AssocationContentPOJO(
    @Json(name = "Id") val id: String,
    @Json(name = "Name") val name: String,
    @Json(name = "NeedSteps") val stepNeed: Long,
    @Json(name = "Balance") val balance: Long,
    @Json(name = "DonationPercent") val percent: Int,
    @Json(name = "ImageUrl") val url: String
)