package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.utils.asAssocationEntityObject
import az.rabita.lifestep.utils.checkNetworkRequestResponse
import java.io.IOException

class AssocationsRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<AssocationsRepository, AppDatabase>(::AssocationsRepository)

    private val assocationsDao = database.assocationsDao
    private val assocationsService = ApiInitHelper.assocationsService

    val listOfAssocations get() = assocationsDao.getAssocationsList()

    suspend fun getAllAssocations(token: String, lang: Int, categoryId: Int): NetworkState = try {
        val response = assocationsService.getAllAssocations(token, lang, categoryId)
        checkNetworkRequestResponse(response).also {
            if (it is NetworkState.Success<*>) {
                val data = response.body()?.content ?: listOf()
                if (data.isNotEmpty()) assocationsDao.cacheAssocations(data.asAssocationEntityObject())
            }
        }
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getAssocationDetails(
        token: String,
        lang: Int,
        assocationId: String
    ): NetworkState = try {
        val response = assocationsService.getDetailsOfAssocation(token, lang, assocationId)
        checkNetworkRequestResponse(response)
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

}