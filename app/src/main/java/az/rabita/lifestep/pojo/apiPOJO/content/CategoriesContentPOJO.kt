package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class CategoriesContentPOJO(
    @Json(name = "Id") val id: Int,
    @Json(name = "Name") val name: String,
    @Json(name = "IsReady") val isReady: Boolean,
    @Json(name = "ImageUrl") val url: String
)