package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthlyStats")
data class MonthlyStat(
    @PrimaryKey val month: Int,
    val monthName: String,
    val count: Long
)