package az.rabita.lifestep.pojo.apiPOJO.status

import com.squareup.moshi.Json

data class StatusPOJO(
        @Json(name = "Code") val code: Int,
        @Json(name = "Text") val text: String,
        @Json(name = "IsSuccess") val isSuccess: Boolean
)