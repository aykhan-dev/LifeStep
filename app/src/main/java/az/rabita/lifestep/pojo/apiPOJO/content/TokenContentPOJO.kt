package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class TokenContentPOJO(
        @Json(name = "token") val token: String
)