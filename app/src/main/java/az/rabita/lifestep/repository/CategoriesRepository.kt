@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.repository

import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.content.CategoriesContentPOJO
import az.rabita.lifestep.utils.asCategoryEntityObject
import az.rabita.lifestep.utils.networkRequestExceptionally

class CategoriesRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<CategoriesRepository, AppDatabase>(::CategoriesRepository)

    private val categoriesDao = database.categoriesDao
    private val categoriesService = ApiInitHelper.categoriesService

    val listOfCategories get() = categoriesDao.getCategoriesList()

    suspend fun getCategories(token: String, lang: Int): NetworkResult =
        networkRequestExceptionally {
            categoriesService.getAllCategories(token, lang)
        }.also {
            if (it is NetworkResult.Success<*>) {
                val data = it.data as List<CategoriesContentPOJO>
                categoriesDao.cacheCategories(data.asCategoryEntityObject())
            }
        }

}