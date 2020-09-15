package az.rabita.lifestep.pojo.dataHolder

import az.rabita.lifestep.pojo.apiPOJO.content.DailyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.MonthlyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.OwnProfileInfoContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.PersonalInfoContentPOJO

data class AllInOneOtherUserInfoHolder(
    val info: PersonalInfoContentPOJO,
    val dailyStats: List<DailyContentPOJO>,
    val monthlyStats: List<MonthlyContentPOJO>
)