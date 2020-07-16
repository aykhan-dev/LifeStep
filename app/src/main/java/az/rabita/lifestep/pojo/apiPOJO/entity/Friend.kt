package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val id: String,
    val surname: String,
    val name: String,
    val balance: Int
)