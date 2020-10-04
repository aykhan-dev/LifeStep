@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.content.AssocationContentPOJO
import az.rabita.lifestep.utils.asAssocationEntityObject
import az.rabita.lifestep.utils.networkRequest

class AssocationsRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<AssocationsRepository, AppDatabase>(::AssocationsRepository)

    private val assocationsDao = database.assocationsDao
    private val assocationsService = ApiInitHelper.assocationsService

    val listOfAssocations get() = assocationsDao.getAssocationsList()

    suspend fun getAllAssocations(token: String, lang: Int, categoryId: Int): NetworkResult =
        networkRequest {
            assocationsService.getAllAssocations(token, lang, categoryId)
        }.also {
            if (it is NetworkResult.Success<*>) {
                val data = it.data as List<AssocationContentPOJO>
                if (data.isNotEmpty()) assocationsDao.cacheAssocations(data.asAssocationEntityObject())
            }
        }

    suspend fun getAssocationDetails(
        token: String,
        lang: Int,
        assocationId: String
    ): NetworkResult = networkRequest {
        assocationsService.getDetailsOfAssocation(token, lang, assocationId)
    }

}