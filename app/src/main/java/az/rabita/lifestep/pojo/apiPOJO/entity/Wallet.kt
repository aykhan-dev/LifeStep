package az.rabita.lifestep.pojo.apiPOJO.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walletInfo")
data class Wallet(
    @PrimaryKey val id: Int = 1,
    val balance: String?,
    val convertSteps: String?,
    val donationSteps: String?,
    val transferStepsIn: String?,
    val transferStepsOut: String?,
    val bonusSteps: String?,
    val balanceMoney: String?
)