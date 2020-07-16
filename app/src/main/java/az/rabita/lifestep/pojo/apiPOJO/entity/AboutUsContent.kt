package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aboutUsContents")
data class AboutUsContent(
    @PrimaryKey val transKey: String,
    val text: String
)