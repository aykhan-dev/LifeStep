package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class OtpContentPOJO(
    @Json(name = "usersId") val userId: String,
    @Json(name = "OTP") val otp: String
)