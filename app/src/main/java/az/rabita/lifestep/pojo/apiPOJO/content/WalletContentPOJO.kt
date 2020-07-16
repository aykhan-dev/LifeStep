package az.rabita.lifestep.pojo.apiPOJO.content

import com.squareup.moshi.Json

data class WalletContentPOJO(
    @Json(name = "Balance") val balance: Long? = 0,
    @Json(name = "ConvertSteps") val convertSteps: Long? = 0,
    @Json(name = "DonationsSteps") val donationSteps: Long? = 0,
    @Json(name = "TransferStepsIn") val transferStepsIn: Long? = 0,
    @Json(name = "TransferStepsOut") val transferStepsOut: Long? = 0,
    @Json(name = "BonusSteps") val bonusSteps: Long? = 0,
    @Json(name = "BalanceAZN") val balanceMoney: Double? = 0.0
)