package az.rabita.lifestep.repository

import android.text.format.DateFormat
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pagingSource.HistoryDonationsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.content.HistoryItemContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.CurrentStepModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DateModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DonateStepModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.SendStepModelPOJO
import az.rabita.lifestep.ui.fragment.history.page.HistoryPageType
import az.rabita.lifestep.utils.NETWORK_PAGE_SIZE
import az.rabita.lifestep.utils.checkNetworkRequestResponse
import java.io.IOException
import java.util.*

object TransactionsRepository {

    private val pagingConfig = PagingConfig(
        pageSize = NETWORK_PAGE_SIZE,
        enablePlaceholders = false
    )

    private val transactionsService = ApiInitHelper.transactionsService

    suspend fun donateStep(
        token: String,
        lang: Int,
        usersId: String,
        count: Long,
        isPrivate: Boolean
    ): NetworkState = try {
        val date = Calendar.getInstance().time
        val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

        val response = transactionsService.donateStep(
            token, lang, DonateStepModelPOJO(
                id = usersId,
                isPrivate = isPrivate,
                count = count,
                createdDate = formatted.toString()
            )
        )

        checkNetworkRequestResponse(response)
    } catch (e: IOException) {
        NetworkState.NetworkException(e.message)
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

    suspend fun transferSteps(token: String, lang: Int, model: SendStepModelPOJO): NetworkState =
        try {
            val response = transactionsService.sendStep(token, lang, model)
            checkNetworkRequestResponse(response)
        } catch (e: Exception) {
            NetworkState.NetworkException(e.message)
        }

    suspend fun sendCurrentStepData(
        token: String,
        lang: Int,
        model: CurrentStepModelPOJO
    ): NetworkState = try {
        val response = transactionsService.sendCurrentStepCount(token, lang, model)
        checkNetworkRequestResponse(response)
    } catch (e: Exception) {
        NetworkState.NetworkException(e.message)
    }

    suspend fun convertSteps(token: String, lang: Int, model: DateModelPOJO): NetworkState =
        try {
            val response = transactionsService.convertSteps(token, lang, model)
            checkNetworkRequestResponse(response)
        } catch (e: Exception) {
            NetworkState.NetworkException(e.message)
        }

    suspend fun getBonusSteps(token: String, lang: Int, model: DateModelPOJO): NetworkState =
        try {
            val response = transactionsService.getBonusSteps(token, lang, model)
            checkNetworkRequestResponse(response)
        } catch (e: Exception) {
            NetworkState.NetworkException(e.message)
        }

}