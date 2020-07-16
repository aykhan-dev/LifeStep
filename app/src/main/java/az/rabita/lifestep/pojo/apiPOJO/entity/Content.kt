package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "contents")
data class Content(
    @PrimaryKey val id: Long,
    val contentKey: String,
    val content: String,
    val imageUrl: String? = "",
    val groupID: Int
)