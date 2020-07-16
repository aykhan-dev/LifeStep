package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weeklyStats")
data class WeeklyStat(
    @PrimaryKey val date: String,
    val weekName: String,
    val isSelected: Boolean,
    val stepCount: Long,
    val convertedSteps: Long,
    val unconvertedSteps: Long,
    val kilometers: Double,
    val calories: Double
)