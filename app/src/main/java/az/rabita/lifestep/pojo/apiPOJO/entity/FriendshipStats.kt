package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friendshipStats")
data class FriendshipStats (
    @PrimaryKey val id: Int = 1,
    val friendsCount: Int,
    val requestCount: Int
)