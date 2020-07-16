package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class DateModelPOJO(
    @Json(name = "createdDate") val date: String
)