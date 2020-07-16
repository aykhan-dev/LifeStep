package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.utils.asFriendshipStatsEntityObject
import az.rabita.lifestep.utils.asWalletEntityObject
import az.rabita.lifestep.utils.asWeeklyStatsEntityObject
import az.rabita.lifestep.utils.checkNetworkRequestResponse
import java.io.IOException

class ReportRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<ReportRepository, AppDatabase>(::ReportRepository)

    private val reportDao = database.reportDao
    private val reportService = ApiInitHelper.reportService

    val walletInfo get() = reportDao.getWalletInfo()
    val friendshipStats get() = reportDao.getFriendshipStats()

    val weeklyStats get() = reportDao.getWeeklyStats()

    suspend fun getTotalTransactionInfo(token: String, lang: Int): NetworkState = try {
        val response = reportService.getTotalTransactionInfo(token, lang)
        checkNetworkRequestResponse(response).also {
            if (it is NetworkState.Success<*>) {
                val data = response.body()?.content ?: listOf()
                if (data.isNotEmpty()) reportDao.cacheWalletInfo(data.asWalletEntityObject()[0])
            }
        }
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getFriendshipStats(token: String, lang: Int): NetworkState = try {
        val response = reportService.getFriendshipStats(token, lang)
        checkNetworkRequestResponse(response).also {
            if (it is NetworkState.Success<*>) {
                val data = response.body()?.content ?: listOf()
                if (data.isNotEmpty()) reportDao.cacheFriendshipStats(data.asFriendshipStatsEntityObject()[0])
            }
        }
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getDailyStats(token: String, lang: Int, userId: String): NetworkState = try {
        val response = reportService.getDailyStats(token, lang, userId)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getMonthlyStats(token: String, lang: Int, userId: String): NetworkState = try {
        val response = reportService.getMonthlyStats(token, lang, userId)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getWeeklyStats(token: String, lang: Int, dateOfToday: String): NetworkState = try {
        val response = reportService.getWeeklyStats(token, lang, dateOfToday)
        checkNetworkRequestResponse(response).also {
            val data = response.body()?.content ?: listOf()
            if (data.isNotEmpty()) reportDao.cacheWeeklyStats(data.asWeeklyStatsEntityObject())
        }
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getChampionsOfWeek(token: String, lang: Int): NetworkState = try {
        val response = reportService.getChampionsOfWeek(token, lang)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

}