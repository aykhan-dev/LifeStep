package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.utils.asNotificationEntityObject
import az.rabita.lifestep.utils.checkNetworkRequestResponse

class NotificationsRepository private constructor(
    database: AppDatabase
) {

    companion object :
        SingletonHolder<NotificationsRepository, AppDatabase>(::NotificationsRepository)

    private val notificationDao = database.notificationsDao
    private val notificationsService = ApiInitHelper.notificationsService

    val listOfNotifications get() = notificationDao.getAllNotifications()

    suspend fun fetchNotifications(token: String, lang: Int): NetworkState = try {
        val response = notificationsService.fetchNotifications(token, lang)
        checkNetworkRequestResponse(response).also {
            if (it is NetworkState.Success<*>) {
                val data = response.body()?.content ?: listOf()
                if (data.isNotEmpty()) {
                    val convertedData = data.asNotificationEntityObject()
                    notificationDao.cacheNotifications(convertedData)
                }
            }
        }
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

}