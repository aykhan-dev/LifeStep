package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactDetails")
data class ContactDetail(
    @PrimaryKey val transKey: String,
    val text: String,
    val url: String?
)