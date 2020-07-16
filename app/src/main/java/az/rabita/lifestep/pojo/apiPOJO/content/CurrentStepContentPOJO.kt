package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class CurrentStepContentPOJO(
    @Json(name = "Date") val date: String,
    @Json(name = "Steps") val steps: Long,
    @Json(name = "ConvertSteps") val convertedSteps: Long,
    @Json(name = "FreeSteps") val unconvertedSteps: Long,
    @Json(name = "Kilometers") val distance: Double,
    @Json(name = "Calories") val calories: Double
)