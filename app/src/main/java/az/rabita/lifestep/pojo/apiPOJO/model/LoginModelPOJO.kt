package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class LoginModelPOJO(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "playerId") val playerId: String
)