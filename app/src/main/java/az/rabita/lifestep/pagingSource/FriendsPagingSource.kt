@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.pagingSource

import androidx.paging.PagingSource
import az.rabita.lifestep.network.FriendshipService
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.content.FriendContentPOJO
import az.rabita.lifestep.utils.networkRequestExceptionally

class FriendsPagingSource(
    private val token: String,
    private val lang: Int,
    private val service: FriendshipService,
    private val onExpiredToken: suspend () -> Unit,
    private val onError: suspend (message: String) -> Unit
) : PagingSource<Int, FriendContentPOJO>() {

    companion object {
        private const val PAGING_START_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FriendContentPOJO> {
        val position: Int = params.key ?: PAGING_START_INDEX

        return try {
            val response = networkRequestExceptionally {
                service.getFriendsList(token, lang, position)
            }
            val items = (response as NetworkResult.Success<List<FriendContentPOJO>>).data
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

}