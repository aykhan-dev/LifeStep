@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.content.NotificationContentPOJO
import az.rabita.lifestep.utils.asNotificationEntityObject
import az.rabita.lifestep.utils.networkRequestExceptionally

class NotificationsRepository private constructor(database: AppDatabase) {

    companion object :
        SingletonHolder<NotificationsRepository, AppDatabase>(::NotificationsRepository)

    private val notificationDao = database.notificationsDao
    private val notificationsService = ApiInitHelper.notificationsService

    val listOfNotifications get() = notificationDao.getAllNotifications()

    suspend fun fetchNotifications(token: String, lang: Int): NetworkResult =
        networkRequestExceptionally {
            notificationsService.fetchNotifications(token, lang)
        }.also {
            if (it is NetworkResult.Success<*>) {
                val data = it.data as List<NotificationContentPOJO>
                if (data.isNotEmpty()) {
                    val convertedData = data.asNotificationEntityObject()
                    notificationDao.cacheNotifications(convertedData)
                }
            }
        }

}