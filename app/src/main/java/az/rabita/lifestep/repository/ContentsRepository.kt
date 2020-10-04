@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.content.ContentContentPOJO
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

    suspend fun getContent(token: String, lang: Int, groupID: Int): NetworkResult = networkRequest {
        contentsService.getContent(token, lang, groupID)
    }.also {
        if (it is NetworkResult.Success<*>) {
            val data = it.data as List<ContentContentPOJO>
            if (data.isNotEmpty()) contentsDao.cacheContents(
                data.asContentEntityObject(groupID),
                groupID
            )
        }
    }

}