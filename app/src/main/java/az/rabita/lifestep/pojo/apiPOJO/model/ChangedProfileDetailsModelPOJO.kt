package az.rabita.lifestep.pojo.apiPOJO.model

import com.squareup.moshi.Json

data class ChangedProfileDetailsModelPOJO (
    @Json(name = "surname") val surname: String,
    @Json(name = "name") val name: String,
    @Json(name = "phoneNumber") val phoneNumber: String
)