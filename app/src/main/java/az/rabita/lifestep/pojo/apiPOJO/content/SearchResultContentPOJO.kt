package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class SearchResultContentPOJO(
    @Json(name = "Id") val id: String,
    @Json(name = "Surname") val surname: String,
    @Json(name = "Name") val name: String,
    @Json(name = "ImageUrl") val imageUrl: String
)