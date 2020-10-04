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
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pagingSource.DonorsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.content.OwnProfileInfoContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.*
import az.rabita.lifestep.pojo.dataHolder.AllInOneOtherUserInfoHolder
import az.rabita.lifestep.pojo.dataHolder.AllInOneUserInfoHolder
import az.rabita.lifestep.utils.NETWORK_PAGE_SIZE
import az.rabita.lifestep.utils.STATIC_TOKEN
import az.rabita.lifestep.utils.asOwnProfileInfoEntityObject
import az.rabita.lifestep.utils.networkRequest
import okhttp3.MultipartBody

class UsersRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<UsersRepository, AppDatabase>(::UsersRepository)

    private val usersDao = database.usersDao
    private val usersService = ApiInitHelper.usersService

    val personalInfo = usersDao.getPersonalInfo()
    val cachedProfileInfo = usersDao.getCachedProfileInfo()

    suspend fun loginUser(lang: Int, model: LoginModelPOJO): NetworkResult = networkRequest {
        usersService.loginUser(lang, STATIC_TOKEN, model)
    }

    suspend fun registerUser(lang: Int, model: RegisterModelPOJO): NetworkResult = networkRequest {
        usersService.registerUser(lang, STATIC_TOKEN, model)
    }

    suspend fun checkEmail(lang: Int, model: CheckEmailModelPOJO): NetworkResult = networkRequest {
        usersService.checkEmail(lang, STATIC_TOKEN, model)
    }

    suspend fun getDonorsOnlyForPost(
        token: String,
        lang: Int,
        model: DonorsModelPOJO
    ): NetworkResult = networkRequest {
        usersService.getDonors(token, lang, model)
    }

    fun getDonorsListStream(
        token: String,
        lang: Int,
        usersId: String,
        onErrorListener: (message: String) -> Unit,
        onExpireTokenListener: () -> Unit
    ): LiveData<PagingData<RankerContentPOJO>> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                DonorsPagingSource(
                    token,
                    lang,
                    usersId,
                    usersService,
                    onErrorListener,
                    onExpireTokenListener
                )
            }
        ).liveData
    }

    suspend fun getPersonalInfo(token: String, lang: Int): NetworkResult = networkRequest {
        usersService.getUserInfo(token, lang)
    }.also {
        if (it is NetworkResult.Success<*>) {
            val data = it.data as List<OwnProfileInfoContentPOJO>
            if (data.isNotEmpty()) usersDao.cachePersonalInfo(data.asOwnProfileInfoEntityObject()[0])
        }
    }

    suspend fun sendOtp(lang: Int, model: EmailModelPOJO): NetworkResult = networkRequest {
        usersService.sendOtp(lang, STATIC_TOKEN, model)
    }

    suspend fun changePassword(
        lang: Int,
        model: ForgotPasswordModelPOJO
    ): NetworkResult = networkRequest {
        usersService.changePassword(lang, STATIC_TOKEN, model)
    }

    suspend fun updatePassword(
        token: String,
        lang: Int,
        model: RefreshPasswordModelPOJO
    ): NetworkResult = networkRequest {
        usersService.updatePassword(token, lang, model)
    }

    suspend fun saveProfileDetailChanges(
        token: String,
        lang: Int,
        model: ChangedProfileDetailsModelPOJO
    ): NetworkResult = networkRequest {
        usersService.saveProfileDetailsChanges(token, lang, model)
    }

    suspend fun changeProfilePhoto(
        token: String,
        lang: Int,
        file: MultipartBody.Part
    ): NetworkResult = networkRequest {
        usersService.changeProfilePicture(token, lang, file)
    }

    suspend fun searchUserByFullName(token: String, lang: Int, query: String): NetworkResult =
        networkRequest {
            usersService.searchByFullName(token, lang, query)
        }

/////////////// DIFFERENT TYPE REQUEST ////////////////

    suspend fun getUserInfoAllInOne(token: String, lang: Int): NetworkResult = try {

        val response = usersService.getUserInfoAllInOne(token, lang)

        val result = if (response.isSuccessful && response.code() == 200) {
            response.body()?.let { body ->
                when (val code = body.status.code) {
                    200, 201 -> NetworkResult.Success(
                        AllInOneUserInfoHolder(
                            body.userInfo[0],
                            body.dailyStats,
                            body.monthlyStats
                        )
                    )
                    else -> NetworkResult.Failure(
                        if (code == 300) NetworkResultFailureType.EXPIRED_TOKEN else NetworkResultFailureType.ERROR,
                        body.status.text
                    )
                }
            } ?: NetworkResult.Failure(NetworkResultFailureType.ERROR, "")
        } else NetworkResult.Failure(NetworkResultFailureType.ERROR, response.message())

        result.also {
            if (it is NetworkResult.Success<*>) {
                val data = (it.data as AllInOneUserInfoHolder).info
                usersDao.cachePersonalInfo(data.asOwnProfileInfoEntityObject())
            }
        }

    } catch (e: Exception) {
        NetworkResult.Failure(NetworkResultFailureType.ERROR, e.message ?: "")
    }

    suspend fun getUserInfoAllInOneById(token: String, lang: Int, usersId: String): NetworkResult =
        try {
            val response = usersService.getUserInfoAllInOneById(token, lang, usersId)

            val result = if (response.isSuccessful && response.code() == 200) {
                response.body()?.let { body ->
                    when (val code = body.status.code) {
                        200, 201 -> NetworkResult.Success(
                            AllInOneOtherUserInfoHolder(
                                body.userInfo[0],
                                body.dailyStats,
                                body.monthlyStats
                            )
                        )
                        else -> NetworkResult.Failure(
                            if (code == 300) NetworkResultFailureType.EXPIRED_TOKEN else NetworkResultFailureType.ERROR,
                            body.status.text
                        )
                    }
                } ?: NetworkResult.Failure(NetworkResultFailureType.ERROR, "")
            } else NetworkResult.Failure(NetworkResultFailureType.ERROR, response.message())

            result
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkResultFailureType.ERROR, e.message ?: "")
        }

}