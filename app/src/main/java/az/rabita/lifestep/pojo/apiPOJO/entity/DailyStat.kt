package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dailyStats")
data class DailyStat(
    @PrimaryKey val createdDate: String,
    val shortName: String,
    val count: Long
)