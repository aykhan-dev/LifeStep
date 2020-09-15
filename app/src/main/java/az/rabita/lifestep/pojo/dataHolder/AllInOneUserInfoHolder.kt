package az.rabita.lifestep.pojo.dataHolder

import az.rabita.lifestep.pojo.apiPOJO.content.DailyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.MonthlyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.OwnProfileInfoContentPOJO

data class AllInOneUserInfoHolder(
    val info: OwnProfileInfoContentPOJO,
    val dailyStats: List<DailyContentPOJO>,
    val monthlyStats: List<MonthlyContentPOJO>
)