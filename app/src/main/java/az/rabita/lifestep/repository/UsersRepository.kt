package az.rabita.lifestep.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pagingSource.DonorsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.*
import az.rabita.lifestep.pojo.dataHolder.AllInOneOtherUserInfoHolder
import az.rabita.lifestep.pojo.dataHolder.AllInOneUserInfoHolder
import az.rabita.lifestep.utils.NETWORK_PAGE_SIZE
import az.rabita.lifestep.utils.STATIC_TOKEN
import az.rabita.lifestep.utils.asOwnProfileInfoEntityObject
import az.rabita.lifestep.utils.checkNetworkRequestResponse
import okhttp3.MultipartBody
import java.io.IOException

class UsersRepository private constructor(database: AppDatabase) {

    companion object : SingletonHolder<UsersRepository, AppDatabase>(::UsersRepository)

    private val usersDao = database.usersDao
    private val usersService = ApiInitHelper.usersService

    val personalInfo get() = usersDao.getPersonalInfo()

    suspend fun loginUser(lang: Int, model: LoginModelPOJO) = try {
        val response = usersService.loginUser(lang, STATIC_TOKEN, model)
        checkNetworkRequestResponse(response)
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun registerUser(lang: Int, model: RegisterModelPOJO) = try {
        val response = usersService.registerUser(lang, STATIC_TOKEN, model)
        checkNetworkRequestResponse(response)
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun checkEmail(lang: Int, model: CheckEmailModelPOJO) = try {
        val response = usersService.checkEmail(lang, STATIC_TOKEN, model)
        checkNetworkRequestResponse(response)
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getDonorsOnlyForPost(token: String, lang: Int, usersId: String): NetworkState =
        try {
            val response = usersService.getDonors(
                token,
                lang,
                DonorsModelPOJO(userId = usersId, pageNumber = 1)
            )
            checkNetworkRequestResponse(response)
        } catch (e: IOException) {
            NetworkState.NetworkException(e.message)
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

    suspend fun getPersonalInfo(token: String, lang: Int): NetworkState = try {
        val response = usersService.getUserInfo(token, lang)
        checkNetworkRequestResponse(response).also {
            if (it is NetworkState.Success<*>) {
                val data = response.body()?.content ?: listOf()
                if (data.isNotEmpty()) usersDao.cachePersonalInfo(data.asOwnProfileInfoEntityObject()[0])
            }
        }
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun sendOtp(lang: Int, model: EmailModelPOJO): NetworkState = try {
        val response = usersService.sendOtp(lang, STATIC_TOKEN, model)
        checkNetworkRequestResponse(response)
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun changePassword(
        lang: Int,
        model: ForgotPasswordModelPOJO
    ): NetworkState = try {
        val response = usersService.changePassword(lang, STATIC_TOKEN, model)
        checkNetworkRequestResponse(response)
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun updatePassword(
        token: String,
        lang: Int,
        model: RefreshPasswordModelPOJO
    ): NetworkState = try {
        val response = usersService.updatePassword(token, lang, model)
        checkNetworkRequestResponse(response)
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun saveProfileDetailChanges(
        token: String,
        lang: Int,
        model: ChangedProfileDetailsModelPOJO
    ): NetworkState = try {
        val response = usersService.saveProfileDetailsChanges(token, lang, model)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun changeProfilePhoto(token: String, lang: Int, file: MultipartBody.Part) = try {
        val response = usersService.changeProfilePicture(token, lang, file)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun searchUserByFullName(token: String, lang: Int, query: String) = try {
        val response = usersService.searchByFullName(token, lang, query)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getUserInfoById(token: String, lang: Int, userId: String): NetworkState = try {
        val response = usersService.getUserInfoById(token, lang, userId)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    /////////////// DIFFERENT TYPE REQUEST ////////////////

    suspend fun getUserInfoAllInOne(token: String, lang: Int): NetworkState = try {
        val response = usersService.getUserInfoAllInOne(token, lang)

        val state = if (response.isSuccessful && response.code() == 200) {
            response.body()?.let { data ->
                when (data.status.code) {
                    200, 201 -> NetworkState.Success(
                        AllInOneUserInfoHolder(
                            data.userInfo[0],
                            data.dailyStats,
                            data.monthlyStats
                        )
                    )
                    300 -> NetworkState.ExpiredToken
                    else -> NetworkState.HandledHttpError(data.status.text)
                }
            } ?: NetworkState.InvalidData
        } else NetworkState.UnhandledHttpError(response.message() + response.code())

        state.also {
            if (it is NetworkState.Success<*>) {
                val data = response.body()?.userInfo ?: listOf()
                if (data.isNotEmpty()) usersDao.cachePersonalInfo(data.asOwnProfileInfoEntityObject()[0])
            }
        }
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun getUserInfoAllInOneById(token: String, lang: Int, usersId: String): NetworkState =
        try {
            val response = usersService.getUserInfoAllInOneById(token, lang, usersId)

            val state = if (response.isSuccessful && response.code() == 200) {
                response.body()?.let { data ->
                    when (data.status.code) {
                        200, 201 -> NetworkState.Success(
                            AllInOneOtherUserInfoHolder(
                                data.userInfo[0],
                                data.dailyStats,
                                data.monthlyStats
                            )
                        )
                        300 -> NetworkState.ExpiredToken
                        else -> NetworkState.HandledHttpError(data.status.text)
                    }
                } ?: NetworkState.InvalidData
            } else NetworkState.UnhandledHttpError(response.message() + response.code())

            state
        } catch (e: Exception) {
            NetworkState.NetworkException(e.message)
        }

}