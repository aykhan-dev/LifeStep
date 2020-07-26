package az.rabita.lifestep.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import az.rabita.lifestep.local.AppDatabase
import az.rabita.lifestep.network.NotificationsService
import az.rabita.lifestep.pojo.apiPOJO.entity.Notifications
import az.rabita.lifestep.utils.asNotificationEntityObject
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class NotificationsRemoteMediator(
    private val token: String,
    private val lang: Int,
    private val database: AppDatabase,
    private val service: NotificationsService
) : RemoteMediator<Int, Notifications>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Notifications>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    lastItem.id
                }
            }

            val response = service.fetchNotifications(token, lang)

            val data = response.body()?.content ?: listOf()
            val convertedData = data.asNotificationEntityObject()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.notificationsDao.deleteNotifications()
                }
                database.notificationsDao.insert(convertedData)
            }

            MediatorResult.Success(endOfPaginationReached = true)

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

}