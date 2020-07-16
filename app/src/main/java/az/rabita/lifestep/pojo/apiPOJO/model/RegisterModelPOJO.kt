package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class RegisterModelPOJO(
    @Json(name = "gender") val gender: Int,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "repeatPassword") val repeatPassword: String,
    @Json(name = "surname") val surname: String,
    @Json(name = "name") val name: String,
    @Json(name = "phoneNumber") val phone: String,
    @Json(name = "invitationCode") val invitationCode: String,
    @Json(name = "playerId") val playerId: String
)