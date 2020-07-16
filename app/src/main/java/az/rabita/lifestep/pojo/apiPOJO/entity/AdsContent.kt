package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adsContents")
data class AdsContent(
    @PrimaryKey val transKey: String,
    val text: String
)