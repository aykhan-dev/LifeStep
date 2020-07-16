package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inviteFriendContents")
data class InviteFriendContent(
    @PrimaryKey val transKey: String,
    val text: String,
    val url: String
)