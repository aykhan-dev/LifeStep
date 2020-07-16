package az.rabita.lifestep.pagingSource

import androidx.paging.PagingSource
import az.rabita.lifestep.network.TransactionsService
import az.rabita.lifestep.pojo.apiPOJO.ServerResponsePOJO
import az.rabita.lifestep.pojo.apiPOJO.content.HistoryItemContentPOJO
import az.rabita.lifestep.ui.fragment.history.page.HistoryPageType
import az.rabita.lifestep.utils.EXPIRE_TOKEN
import az.rabita.lifestep.utils.NOT_SUCCESSFUL
import az.rabita.lifestep.utils.NO_INTERNET_CONNECTION
import az.rabita.lifestep.utils.NULL_BODY
import retrofit2.Response

private const val HISTORY_DONATIONS_STARTING_PAGE_INDEX = 1

class HistoryDonationsPagingSource(
    private val token: String,
    private val lang: Int,
    private val pageType: HistoryPageType,
    private val service: TransactionsService,
    private val onErrorListener: (message: String) -> Unit,
    private val onExpireTokenListener: () -> Unit
) : PagingSource<Int, HistoryItemContentPOJO>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HistoryItemContentPOJO> {
        val position = params.key ?: HISTORY_DONATIONS_STARTING_PAGE_INDEX
        return try {
            val response = request(position)
            if (response.isSuccessful) {
                if (response.body() != null) {
                    val body = response.body()!!
                    if (body.status.code != 300) {
                        val items = response.body()!!.content ?: listOf()
                        LoadResult.Page(
                            data = items,
                            prevKey = if (position == HISTORY_DONATIONS_STARTING_PAGE_INDEX) null else position - 1,
                            nextKey = if (items.isEmpty()) null else position + 1
                        )
                    } else {
                        onExpireTokenListener()
                        LoadResult.Error(Exception(EXPIRE_TOKEN))
                    }
                } else {
                    LoadResult.Error(Exception(NULL_BODY))
                }
            } else {
                LoadResult.Error(Exception(NOT_SUCCESSFUL))
            }

        } catch (exception: Exception) {
            onErrorListener(NO_INTERNET_CONNECTION)
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