package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json


data class AdsTransactionContentPOJO(
    @Json(name = "Description") val description: String,
    @Json(name = "LogoUrl") val logoUrl: String,
    @Json(name = "SubTitle") val subTitle: String,
    @Json(name = "Time") val time: Int,
    @Json(name = "Title") val title: String,
    @Json(name = "TransactionId") val transactionId: String,
    @Json(name = "Url") val url: String,
    @Json(name = "VideoUrl") val videoUrl: String
)