package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.utils.asCategoryEntityObject
import az.rabita.lifestep.utils.checkNetworkRequestResponse
import okio.IOException

class CategoriesRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<CategoriesRepository, AppDatabase>(::CategoriesRepository)

    private val categoriesDao = database.categoriesDao
    private val categoriesService = ApiInitHelper.categoriesService

    val listOfCategories get() = categoriesDao.getCategoriesList()

    suspend fun getCategories(token: String, lang: Int): NetworkState = try {
        val response = categoriesService.getAllCategories(token, lang)
        checkNetworkRequestResponse(response).also {
            if (it is NetworkState.Success<*>) {
                val data = response.body()!!.content ?: listOf()
                categoriesDao.cacheCategories(data.asCategoryEntityObject())
            }
        }
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

}