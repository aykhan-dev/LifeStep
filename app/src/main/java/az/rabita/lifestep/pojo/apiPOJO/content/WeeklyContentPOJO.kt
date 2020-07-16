package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class WeeklyContentPOJO(
    @Json(name = "CreatedDate") val date: String,
    @Json(name = "ShortName") val weekName: String,
    @Json(name = "Selected") val isSelected: Boolean,
    @Json(name = "Steps") val stepCount: Long,
    @Json(name = "ConvertSteps") val convertedSteps: Long,
    @Json(name = "FreeSteps") val unconvertedSteps: Long,
    @Json(name = "Kilometers") val kilometers: Double,
    @Json(name = "Calories") val calories: Double
)