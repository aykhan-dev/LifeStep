package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assocations")
data class Assocation(
    @PrimaryKey val id: String,
    val name: String,
    val stepNeed: String, //850000000 = 850 mln
    val balance: String, //850000000 = 850 mln
    val percent: Int,
    val url: String
)