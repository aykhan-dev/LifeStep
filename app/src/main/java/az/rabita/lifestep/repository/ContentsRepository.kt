package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.utils.*

class ContentsRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<ContentsRepository, AppDatabase>(::ContentsRepository)

    private val contentsDao = database.allContentsDao
    private val contentsService = ApiInitHelper.contentsService

    val adsContentTitle = contentsDao.getContent(ADS_GROUP_ID, TITLE_KEY)
    val adsContentBody = contentsDao.getContent(ADS_GROUP_ID, CONTENT_TEXT_KEY)

    val aboutUsContentBody = contentsDao.getContent(ABOUT_US_GROUP_ID, CONTENT_TEXT_KEY)

    val contactDetails = contentsDao.getContents(CONTACTS_GROUP_ID)

    val inviteFriendsContentBody =
        contentsDao.getContentAsFlow(INVITE_FRIENDS_GROUP_ID, CONTENT_TEXT_KEY)

    val inviteFriendsContentMessage =
        contentsDao.getContent(INVITE_FRIENDS_GROUP_ID, INVITE_TEXT_KEY)

    suspend fun getContent(token: String, lang: Int, groupID: Int): NetworkState = try {
        val response = contentsService.getContent(token, lang, groupID)
        checkNetworkRequestResponse(response).also {
            if (it is NetworkState.Success<*>) {
                val data = response.body()?.content ?: listOf()
                if (data.isNotEmpty()) contentsDao.cacheContents(
                    data.asContentEntityObject(groupID),
                    groupID
                )
//                    else {
//                        val list = data.asContentEntityObject(INVITE_FRIENDS_GROUP_ID)
//                        contentsDao.cacheContent(list[0], INVITE_FRIENDS_GROUP_ID, CONTENT_TEXT_KEY)
//                        contentsDao.cacheContent(list[1], INVITE_FRIENDS_GROUP_ID, INVITE_TEXT_KEY)
//                    }
            }
        }
    } catch (exception: Exception) {
        NetworkState.NetworkException(exception.message)
    }

}