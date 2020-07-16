package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personalInfo")
data class PersonalInfo(
    @PrimaryKey val saveId: Int = 1,
    val id: String,
    val fullName: String,
    val surname: String,
    val name: String,
    val email: String,
    val phone: String,
    val url: String,
    val balance: Long,
    val friendsCount: Int,
    val invitationCode: String,
    val createdDate: String
)