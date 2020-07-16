package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class EmailModelPOJO(
    @Json(name = "email") val email: String
)