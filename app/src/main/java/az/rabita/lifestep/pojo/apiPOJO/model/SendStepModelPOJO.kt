package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class SendStepModelPOJO(
    @Json(name = "usersId") val userId: String,
    @Json(name = "count") val count: Long,
    @Json(name = "createdDate") val date: String
)