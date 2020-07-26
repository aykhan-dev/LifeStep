package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class ConvertStepsModelPOJO(
    @Json(name = "createdDate") val createdDate: String,
    @Json(name = "transactionId") val transactionId: String,
    @Json(name = "watchTime") val watchTime: Int
)