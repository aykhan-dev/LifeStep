@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.pagingSource

import androidx.paging.PagingSource
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.UsersService
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DonorsModelPOJO
import az.rabita.lifestep.utils.networkRequestExceptionally

class DonorsPagingSource(
    private val token: String,
    private val lang: Int,
    private val usersId: String,
    private val service: UsersService,
    private val onExpiredToken: suspend () -> Unit,
    private val onError: suspend (String) -> Unit
) : PagingSource<Int, RankerContentPOJO>() {

    companion object {
        private const val PAGING_START_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RankerContentPOJO> {

        val position: Int = params.key ?: PAGING_START_INDEX

        return try {
            val response = networkRequestExceptionally {
                service.getDonors(token, lang, DonorsModelPOJO(usersId, position))
            }
            val items = (response as NetworkResult.Success<List<RankerContentPOJO>>).data
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