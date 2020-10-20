package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class CurrentStepModelPOJO(
    @Json(name = "calorie") val calorie: Float,
    @Json(name = "kilometer") val kilometer: Float,
    @Json(name = "count") val count: Long,
    @Json(name = "createdDate") val createdDate: String
)