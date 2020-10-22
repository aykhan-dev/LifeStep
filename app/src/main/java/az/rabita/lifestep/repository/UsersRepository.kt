@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pagingSource.DonorsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.content.OwnProfileInfoContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.*
import az.rabita.lifestep.pojo.dataHolder.AllInOneOtherUserInfoHolder
import az.rabita.lifestep.pojo.dataHolder.AllInOneUserInfoHolder
import az.rabita.lifestep.utils.NETWORK_PAGE_SIZE
import az.rabita.lifestep.utils.STATIC_TOKEN
import az.rabita.lifestep.utils.asOwnProfileInfoEntityObject
import az.rabita.lifestep.utils.networkRequestExceptionally
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class UsersRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<UsersRepository, AppDatabase>(::UsersRepository)

    private val usersDao = database.usersDao
    private val usersService = ApiInitHelper.usersService

    val personalInfo = usersDao.getPersonalInfo()
    val cachedProfileInfo = usersDao.getCachedProfileInfo()

    suspend fun loginUser(lang: Int, model: LoginModelPOJO): NetworkResult =
        networkRequestExceptionally {
            usersService.loginUser(lang, STATIC_TOKEN, model)
        }

    suspend fun registerUser(lang: Int, model: RegisterModelPOJO): NetworkResult =
        networkRequestExceptionally {
            usersService.registerUser(lang, STATIC_TOKEN, model)
        }

    suspend fun checkEmail(lang: Int, model: CheckEmailModelPOJO): NetworkResult =
        networkRequestExceptionally {
            usersService.checkEmail(lang, STATIC_TOKEN, model)
        }

    suspend fun getDonorsOnlyForPost(
        token: String,
        lang: Int,
        model: DonorsModelPOJO
    ): NetworkResult = networkRequestExceptionally {
        usersService.getDonors(token, lang, model)
    }

    fun getDonorsListStream(
        token: String,
        lang: Int,
        usersId: String,
        onErrorListener: suspend (message: String) -> Unit,
        onExpireTokenListener: suspend () -> Unit
    ): LiveData<PagingData<RankerContentPOJO>> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                DonorsPagingSource(
                    token,
                    lang,
                    usersId,
                    usersService,
                    onExpireTokenListener,
                    onErrorListener
                )
            }
        ).liveData
    }

    suspend fun getPersonalInfo(token: String, lang: Int): NetworkResult =
        networkRequestExceptionally {
            usersService.getUserInfo(token, lang)
        }.also {
            if (it is NetworkResult.Success<*>) {
                val data = it.data as List<OwnProfileInfoContentPOJO>
                if (data.isNotEmpty()) usersDao.cachePersonalInfo(data.asOwnProfileInfoEntityObject()[0])
            }
        }

    suspend fun getPersonalInfoExceptionally(token: String, lang: Int): NetworkResult =
        networkRequestExceptionally {
            usersService.getUserInfo(token, lang)
        }.also {
            if (it is NetworkResult.Success<*>) {
                val data = it.data as List<OwnProfileInfoContentPOJO>
                if (data.isNotEmpty()) usersDao.cachePersonalInfo(data.asOwnProfileInfoEntityObject()[0])
            }
        }

    suspend fun sendOtp(lang: Int, model: EmailModelPOJO): NetworkResult =
        networkRequestExceptionally {
            usersService.sendOtp(lang, STATIC_TOKEN, model)
        }

    suspend fun changePassword(
        lang: Int,
        model: ForgotPasswordModelPOJO
    ): NetworkResult = networkRequestExceptionally {
        usersService.changePassword(lang, STATIC_TOKEN, model)
    }

    suspend fun updatePassword(
        token: String,
        lang: Int,
        model: RefreshPasswordModelPOJO
    ): NetworkResult = networkRequestExceptionally {
        usersService.updatePassword(token, lang, model)
    }

    suspend fun saveProfileDetailChanges(
        token: String,
        lang: Int,
        model: ChangedProfileDetailsModelPOJO
    ): NetworkResult = networkRequestExceptionally {
        usersService.saveProfileDetailsChanges(token, lang, model)
    }

    suspend fun changeProfilePhoto(
        token: String,
        lang: Int,
        file: MultipartBody.Part
    ): NetworkResult = networkRequestExceptionally {
        usersService.changeProfilePicture(token, lang, file)
    }

    suspend fun searchUserByFullName(token: String, lang: Int, query: String) =
        networkRequestExceptionally {
            usersService.searchByFullName(token, lang, query)
        }

/////////////// DIFFERENT TYPE REQUEST ////////////////

    suspend fun getOwnProfileInfo(token: String, lang: Int): NetworkResult =
        withContext(Dispatchers.IO) {
            val response = usersService.getUserInfoAllInOne(token, lang)
            return@withContext if (response.isSuccessful && response.code() == 200 && response.body() != null) {
                val body = response.body()!!
                when (val code = body.status.code) {
                    200 -> NetworkResult.Success(
                        AllInOneUserInfoHolder(
                            body.userInfo[0],
                            body.dailyStats,
                            body.monthlyStats
                        )
                    ).also {
                        val data = it.data.info
                        usersDao.cachePersonalInfo(data.asOwnProfileInfoEntityObject())
                    }
                    else -> throw when (code) {
                        201 -> NetworkResult.Exceptions.RepeatedAction(code, body.status.text)
                        300 -> NetworkResult.Exceptions.ExpiredToken(code, body.status.text)
                        else -> NetworkResult.Exceptions.Failure(code, body.status.text)
                    }
                }
            } else throw NetworkResult.Exceptions.ServerError(response.code(), response.message())
        }

    suspend fun getUserInfo(token: String, lang: Int, usersId: String): NetworkResult =
        withContext(Dispatchers.IO) {
            val response = usersService.getUserInfoAllInOneById(token, lang, usersId)
            return@withContext if (response.isSuccessful && response.code() == 200 && response.body() != null) {
                val body = response.body()!!
                when (val code = body.status.code) {
                    200 -> NetworkResult.Success(
                        AllInOneOtherUserInfoHolder(
                            body.userInfo[0],
                            body.dailyStats,
                            body.monthlyStats
                        )
                    )
                    else -> throw when (code) {
                        201 -> NetworkResult.Exceptions.RepeatedAction(code, body.status.text)
                        300 -> NetworkResult.Exceptions.ExpiredToken(code, body.status.text)
                        else -> NetworkResult.Exceptions.Failure(code, body.status.text)
                    }
                }
            } else throw NetworkResult.Exceptions.ServerError(response.code(), response.message())
        }

}