package az.rabita.lifestep.repository

import androidx.paging.PagingConfig
import az.rabita.lifestep.manager.SingletonHolder
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.model.FriendRequestModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendshipActionModelPOJO
import az.rabita.lifestep.utils.NETWORK_PAGE_SIZE
import az.rabita.lifestep.utils.PaginationListeners
import az.rabita.lifestep.utils.checkNetworkRequestResponse

object FriendshipRepository {

    private val friendShipService = ApiInitHelper.friendshipService

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