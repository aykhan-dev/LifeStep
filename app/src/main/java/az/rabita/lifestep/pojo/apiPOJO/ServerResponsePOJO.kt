package az.rabita.lifestep.pojo.apiPOJO

import az.rabita.lifestep.pojo.apiPOJO.status.StatusPOJO
import com.squareup.moshi.Json

data class ServerResponsePOJO<ContentObject>(
    @Json(name = "Status") val status: StatusPOJO,
    @Json(name = "Content") val content: List<ContentObject>? = null
)