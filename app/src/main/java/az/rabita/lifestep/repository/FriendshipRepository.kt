package az.rabita.lifestep.repository

import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.model.FriendRequestModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendshipActionModelPOJO
import az.rabita.lifestep.utils.networkRequest

object FriendshipRepository {

    private val friendShipService = ApiInitHelper.friendshipService

    suspend fun processFriendRequest(
        token: String,
        lang: Int,
        friendshipActionModelPOJO: FriendshipActionModelPOJO,
        isAccepted: Boolean
    ): NetworkResult = networkRequest {
        if (isAccepted)
            friendShipService.acceptFriendRequest(token, lang, friendshipActionModelPOJO)
        else
            friendShipService.rejectFriendRequest(token, lang, friendshipActionModelPOJO)
    }

    suspend fun sendFriendRequest(
        token: String,
        lang: Int,
        model: FriendRequestModelPOJO
    ): NetworkResult = networkRequest {
        friendShipService.sendFriendRequest(token, lang, model)
    }

}