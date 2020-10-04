@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.content.FriendshipContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.WalletContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.WeeklyContentPOJO
import az.rabita.lifestep.utils.asFriendshipStatsEntityObject
import az.rabita.lifestep.utils.asWalletEntityObject
import az.rabita.lifestep.utils.asWeeklyStatsEntityObject
import az.rabita.lifestep.utils.networkRequest

class ReportRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<ReportRepository, AppDatabase>(::ReportRepository)

    private val reportDao = database.reportDao
    private val reportService = ApiInitHelper.reportService

    val walletInfo get() = reportDao.getWalletInfo()
    val friendshipStats get() = reportDao.getFriendshipStats()

    val weeklyStats get() = reportDao.getWeeklyStats()

    suspend fun getTotalTransactionInfo(token: String, lang: Int): NetworkResult = networkRequest {
        reportService.getTotalTransactionInfo(token, lang)
    }.also {
        if (it is NetworkResult.Success<*>) {
            val data = it.data as List<WalletContentPOJO>
            if (data.isNotEmpty()) reportDao.cacheWalletInfo(data.asWalletEntityObject()[0])
        }
    }

    suspend fun getFriendshipStats(token: String, lang: Int): NetworkResult = networkRequest {
        reportService.getFriendshipStats(token, lang)
    }.also {
        if (it is NetworkResult.Success<*>) {
            val data = it.data as List<FriendshipContentPOJO>
            if (data.isNotEmpty()) reportDao.cacheFriendshipStats(data.asFriendshipStatsEntityObject()[0])
        }
    }

    suspend fun getWeeklyStats(token: String, lang: Int, dateOfToday: String): NetworkResult =
        networkRequest {
            reportService.getWeeklyStats(token, lang, dateOfToday)
        }.also {
            if (it is NetworkResult.Success<*>) {
                val data = it.data as List<WeeklyContentPOJO>
                if (data.isNotEmpty()) reportDao.cacheWeeklyStats(data.asWeeklyStatsEntityObject())
            }
        }

    suspend fun getChampionsOfDay(token: String, lang: Int): NetworkResult = networkRequest {
        reportService.getChampionsOfDay(token, lang)
    }

    suspend fun getChampionsOfWeek(token: String, lang: Int): NetworkResult = networkRequest {
        reportService.getChampionsOfWeek(token, lang)
    }

    suspend fun getChampionsOfMonth(token: String, lang: Int): NetworkResult = networkRequest {
        reportService.getChampionsOfMonth(token, lang)
    }

}