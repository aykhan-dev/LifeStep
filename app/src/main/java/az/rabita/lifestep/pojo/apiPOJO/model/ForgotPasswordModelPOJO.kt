package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class ForgotPasswordModelPOJO(
    @Json(name = "usersId") val id: String,
    @Json(name = "otp") val otp: String,
    @Json(name = "password") val password: String,
    @Json(name = "repeatPassword") val confirmPassword: String
)