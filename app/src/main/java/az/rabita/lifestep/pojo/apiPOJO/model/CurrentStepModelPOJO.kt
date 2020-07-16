package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class CurrentStepModelPOJO(
    @Json(name = "count") val count: Long,
    @Json(name = "createdDate") val createdDate: String
)