package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class RefreshPasswordModelPOJO(
    @Json(name = "password") val password: String,
    @Json(name = "repeatPassword") val confirmPassword: String
)