package az.rabita.lifestep.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pagingSource.HistoryDonationsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.content.HistoryItemContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.CurrentStepModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DateModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DonateStepModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.SendStepModelPOJO
import az.rabita.lifestep.ui.fragment.history.page.HistoryPageType
import az.rabita.lifestep.utils.NETWORK_PAGE_SIZE
import az.rabita.lifestep.utils.networkRequest

object TransactionsRepository {

    private val pagingConfig = PagingConfig(
        pageSize = NETWORK_PAGE_SIZE,
        enablePlaceholders = false
    )

    private val transactionsService = ApiInitHelper.transactionsService

    suspend fun donateStep(
        token: String,
        lang: Int,
        model: DonateStepModelPOJO
    ): NetworkResult = networkRequest {
        transactionsService.donateStep(token, lang, model)
    }

    fun getHistoryDonationsListStream(
        token: String,
        lang: Int,
        pageType: HistoryPageType,
        onErrorListener: (message: String) -> Unit,
        onExpireTokenListener: () -> Unit
    ): LiveData<PagingData<HistoryItemContentPOJO>> {
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                HistoryDonationsPagingSource(
                    token,
                    lang,
                    pageType,
                    transactionsService,
                    onErrorListener,
                    onExpireTokenListener
                )
            }
        ).liveData
    }

    suspend fun transferSteps(token: String, lang: Int, model: SendStepModelPOJO): NetworkResult =
        networkRequest {
            transactionsService.sendStep(token, lang, model)
        }

    suspend fun sendCurrentStepData(
        token: String,
        lang: Int,
        model: CurrentStepModelPOJO
    ): NetworkResult = networkRequest {
        transactionsService.sendCurrentStepCount(token, lang, model)
    }

    suspend fun convertSteps(token: String, lang: Int, model: DateModelPOJO): NetworkResult =
        networkRequest {
            transactionsService.convertSteps(token, lang, model)
        }

    suspend fun getBonusSteps(token: String, lang: Int, model: DateModelPOJO): NetworkResult =
        networkRequest {
            transactionsService.getBonusSteps(token, lang, model)
        }

}