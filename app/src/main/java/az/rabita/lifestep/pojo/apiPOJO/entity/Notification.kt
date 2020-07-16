package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: Long,
    val imageUrl: String,
    val messages: String,
    val title: String
)