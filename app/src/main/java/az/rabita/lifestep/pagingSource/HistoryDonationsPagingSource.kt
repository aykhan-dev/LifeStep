@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.pagingSource

import androidx.paging.PagingSource
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.TransactionsService
import az.rabita.lifestep.pojo.apiPOJO.ServerResponsePOJO
import az.rabita.lifestep.pojo.apiPOJO.content.HistoryItemContentPOJO
import az.rabita.lifestep.ui.fragment.history.page.HistoryPageType
import az.rabita.lifestep.utils.networkRequestExceptionally
import retrofit2.Response

class HistoryDonationsPagingSource(
    private val token: String,
    private val lang: Int,
    private val pageType: HistoryPageType,
    private val service: TransactionsService,
    private val onExpiredToken: suspend () -> Unit,
    private val onError: suspend (String) -> Unit
) : PagingSource<Int, HistoryItemContentPOJO>() {

    companion object {
        private const val PAGING_START_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HistoryItemContentPOJO> {
        val position = params.key ?: PAGING_START_INDEX
        return try {
            val response = networkRequestExceptionally {
                request(position)
            }
            val items = (response as NetworkResult.Success<List<HistoryItemContentPOJO>>).data
            LoadResult.Page(
                data = items,
                prevKey = if (position == PAGING_START_INDEX) null else position - 1,
                nextKey = if (items.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            when (exception) {
                is NetworkResult.Exceptions.ExpiredToken -> onExpiredToken()
                else -> onError(exception.message ?: "")
            }
            LoadResult.Error(exception)
        }
    }

    private suspend fun request(position: Int): Response<ServerResponsePOJO<HistoryItemContentPOJO>> {
        return when (pageType) {
            HistoryPageType.DONATIONS -> service.fetchDonations(token, lang, position)
            HistoryPageType.TRANSFERS -> service.fetchTransfers(token, lang, position)
            HistoryPageType.EARNED -> service.fetchBonus(token, lang, position)
        }
    }

}