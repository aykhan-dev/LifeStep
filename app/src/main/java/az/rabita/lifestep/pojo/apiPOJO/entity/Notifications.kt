package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "notifications")
data class Notifications(
    @PrimaryKey
    @ColumnInfo(name = "Id") val id: String,
    @ColumnInfo(name = "ImageUrl") val imageUrl: String,
    @ColumnInfo(name = "Messages") val messages: String,
    @ColumnInfo(name = "Title") val title: String,
    @ColumnInfo(name = "Url") val url: String,
    @ColumnInfo(name = "UsersId") val usersId: String,
    @ColumnInfo(name = "UsersNotificationsTypesId") val usersNotificationsTypesId: Int
)