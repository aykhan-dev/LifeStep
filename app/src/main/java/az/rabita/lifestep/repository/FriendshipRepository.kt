package az.rabita.lifestep.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pagingSource.FriendRequestsPagingSource
import az.rabita.lifestep.pagingSource.FriendsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.content.FriendContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendRequestModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendshipActionModelPOJO
import az.rabita.lifestep.utils.NETWORK_PAGE_SIZE
import az.rabita.lifestep.utils.PaginationListeners
import az.rabita.lifestep.utils.checkNetworkRequestResponse
import kotlinx.coroutines.flow.Flow

class FriendshipRepository private constructor(private val paginationListeners: PaginationListeners) {

    companion object :
        SingletonHolder<FriendshipRepository, PaginationListeners>(::FriendshipRepository)

    private val pagingConfig =
        PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false)

    private val friendShipService = ApiInitHelper.friendshipService

    fun getFriendsListStream(token: String, lang: Int): LiveData<PagingData<FriendContentPOJO>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                FriendsPagingSource(
                    token,
                    lang,
                    friendShipService,
                    paginationListeners.onErrorListener,
                    paginationListeners.onExpireTokenListener
                )
            }
        ).liveData
    }

    fun getFriendRequestsListStream(
        token: String,
        lang: Int
    ): LiveData<PagingData<FriendContentPOJO>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                FriendRequestsPagingSource(
                    token,
                    lang,
                    friendShipService,
                    paginationListeners.onErrorListener,
                    paginationListeners.onExpireTokenListener
                )
            }
        ).liveData
    }

    suspend fun processFriendRequest(
        token: String,
        lang: Int,
        friendshipActionModelPOJO: FriendshipActionModelPOJO,
        isAccepted: Boolean
    ): NetworkState = try {
        if (isAccepted) {
            val response =
                friendShipService.acceptFriendRequest(token, lang, friendshipActionModelPOJO)
            checkNetworkRequestResponse(response)
        } else {
            val response =
                friendShipService.rejectFriendRequest(token, lang, friendshipActionModelPOJO)
            checkNetworkRequestResponse(response)
        }
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun sendFriendRequest(
        token: String,
        lang: Int,
        model: FriendRequestModelPOJO
    ): NetworkState = try {
        val response = friendShipService.sendFriendRequest(token, lang, model)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

}