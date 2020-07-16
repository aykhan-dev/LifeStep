package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class DonorsModelPOJO(
    @Json(name = "usersId") val userId: String,
    @Json(name = "pageNumber") val pageNumber: Int
)