package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Notification(
    val id: Long,
    val imageUrl: String,
    val messages: String,
    val title: String
)