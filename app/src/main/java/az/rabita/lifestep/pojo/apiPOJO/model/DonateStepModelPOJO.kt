package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class DonateStepModelPOJO (
    @Json(name = "usersId") val id: String,
    @Json(name = "IsPrivate") val isPrivate: Boolean,
    @Json(name = "count") val count: Long,
    @Json(name = "createdDate") val createdDate: String
)