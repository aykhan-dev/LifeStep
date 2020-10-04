package az.rabita.lifestep.repository

import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.model.ConvertStepsModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DateModelPOJO
import az.rabita.lifestep.utils.networkRequest

object AdsRepository {

    private val advertisementService = ApiInitHelper.advertisementsService

    suspend fun createAdsTransaction(
        token: String,
        lang: Int,
        model: DateModelPOJO
    ): NetworkResult = networkRequest {
        advertisementService.createAdsTransaction(token, lang, model)
    }

    suspend fun sendAdsTransactionResult(
        token: String,
        lang: Int,
        model: ConvertStepsModelPOJO
    ): NetworkResult = networkRequest {
        advertisementService.sendAdsTransactionResult(token, lang, model)
    }

    suspend fun sendBonusAdsTransactionResult(
        token: String,
        lang: Int,
        model: ConvertStepsModelPOJO
    ): NetworkResult = networkRequest {
        advertisementService.sendBonusAdsTransactionResult(token, lang, model)
    }

}