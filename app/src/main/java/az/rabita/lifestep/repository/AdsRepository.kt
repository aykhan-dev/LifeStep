package az.rabita.lifestep.repository

import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.model.ConvertStepsModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DateModelPOJO
import az.rabita.lifestep.utils.checkNetworkRequestResponse

object AdsRepository {

    private val advertisementService = ApiInitHelper.advertisementsService

    suspend fun createAdsTransaction(token: String, lang: Int, model: DateModelPOJO): NetworkState =
        try {
            val response = advertisementService.createAdsTransaction(token, lang, model)
            checkNetworkRequestResponse(response)
        } catch (e: Exception) {
            NetworkState.NetworkException(e.message)
        }

    suspend fun sendAdsTransactionResult(
        token: String,
        lang: Int,
        model: ConvertStepsModelPOJO
    ): NetworkState = try {
        val response = advertisementService.sendAdsTransactionResult(token, lang, model)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun sendBonusAdsTransactionResult(
        token: String,
        lang: Int,
        model: ConvertStepsModelPOJO
    ): NetworkState = try {
        val response = advertisementService.sendBonusAdsTransactionResult(token, lang, model)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

}