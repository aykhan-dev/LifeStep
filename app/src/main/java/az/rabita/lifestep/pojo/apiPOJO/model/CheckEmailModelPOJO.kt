package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class CheckEmailModelPOJO(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "repeatPassword") val passwordConfirm: String
)