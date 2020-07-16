package az.rabita.lifestep.pagingSource

import androidx.paging.PagingSource
import az.rabita.lifestep.network.UsersService
import az.rabita.lifestep.pojo.apiPOJO.content.DonorsContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DonorsModelPOJO
import az.rabita.lifestep.utils.EXPIRE_TOKEN
import az.rabita.lifestep.utils.NOT_SUCCESSFUL
import az.rabita.lifestep.utils.NO_INTERNET_CONNECTION
import az.rabita.lifestep.utils.NULL_BODY

private const val DONORS_STARTING_PAGE_INDEX = 1

class DonorsPagingSource(
    private val token: String,
    private val lang: Int,
    private val usersId: String,
    private val service: UsersService,
    private val onErrorListener: (message: String) -> Unit,
    private val onExpireTokenListener: () -> Unit
) : PagingSource<Int, RankerContentPOJO>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RankerContentPOJO> {
        val position = params.key ?: DONORS_STARTING_PAGE_INDEX
        return try {
            val response = service.getDonors(
                token, lang, DonorsModelPOJO(
                    userId = usersId,
                    pageNumber = position
                )
            )

            if (response.isSuccessful) {
                if (response.body() != null) {
                    val body = response.body()!!
                    if (body.status.code != 300) {
                        val items = body.content ?: listOf()
                        LoadResult.Page(
                            data = items,
                            prevKey = if (position == DONORS_STARTING_PAGE_INDEX) null else position - 1,
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

}