package az.rabita.lifestep.local

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import az.rabita.lifestep.pojo.apiPOJO.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UsersDao {

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(item: PersonalInfo)

    @Query("select * from personalInfo where saveId = 1")
    abstract fun getPersonalInfo(): Flow<PersonalInfo>

    @Query("delete from personalInfo")
    abstract suspend fun deletePersonalInfo()

    @Query("delete from personalInfo")
    abstract fun deletePersonalInfoSync()

    @Transaction
    open suspend fun cachePersonalInfo(item: PersonalInfo) {
        deletePersonalInfo()
        insert(item)
    }

}

@Dao
abstract class CategoriesDao {

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(items: List<Category>)

    @Query("select * from categories")
    abstract fun getCategoriesList(): Flow<List<Category>>

    @Query("delete from categories")
    abstract suspend fun deleteAllCategories()

    @Query("delete from categories")
    abstract fun deleteAllCategoriesSync()

    @Transaction
    open suspend fun cacheCategories(items: List<Category>) {
        deleteAllCategories()
        insert(items)
    }

}

@Dao
abstract class AssocationsDao {

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(items: List<Assocation>)

    @Query("select * from assocations")
    abstract fun getAssocationsList(): Flow<List<Assocation>>

    @Query("delete from assocations")
    abstract suspend fun deleteAllAssocations()

    @Query("delete from assocations")
    abstract fun deleteAllAssocationsSync()

    @Transaction
    open suspend fun cacheAssocations(items: List<Assocation>) {
        deleteAllAssocations()
        insert(items)
    }

}

@Dao
abstract class ReportDao {

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(item: Wallet)

    @Query("select * from walletInfo where id = 1")
    abstract fun getWalletInfo(): Flow<Wallet>

    @Query("delete from walletInfo")
    abstract suspend fun deleteWalletInfo()

    @Query("delete from walletInfo")
    abstract fun deleteWalletInfoSync()

    @Transaction
    open suspend fun cacheWalletInfo(item: Wallet) {
        deleteWalletInfo()
        insert(item)
    }

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(item: FriendshipStats)

    @Query("select * from friendshipStats where id = 1")
    abstract fun getFriendshipStats(): Flow<FriendshipStats>

    @Query("delete from friendshipStats")
    abstract suspend fun deleteAllFriendshipStats()

    @Query("delete from friendshipStats")
    abstract fun deleteAllFriendshipStatsSync()

    @Transaction
    open suspend fun cacheFriendshipStats(item: FriendshipStats) {
        deleteAllFriendshipStats()
        insert(item)
    }

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(items: List<WeeklyStat>)

    @Query("select * from weeklyStats")
    abstract fun getWeeklyStats(): Flow<List<WeeklyStat>>

    @Query("delete from weeklyStats")
    abstract suspend fun deleteAllWeeklyStats()

    @Query("delete from weeklyStats")
    abstract fun deleteAllWeeklyStatsSync()

    @Transaction
    open suspend fun cacheWeeklyStats(items: List<WeeklyStat>) {
        deleteAllWeeklyStats()
        insert(items)
    }

}

@Dao
abstract class ContentsDao {

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(items: List<Content>)

    @Query("select * from contents where groupID = :groupId")
    abstract fun getContents(groupId: Int): LiveData<List<Content>>

    @Query("delete from contents where groupID = :groupId")
    abstract suspend fun deleteContents(groupId: Int)

    @Query("delete from contents where groupID = :groupId")
    abstract fun deleteContentsSync(groupId: Int)

    @Transaction
    open suspend fun cacheContents(items: List<Content>, groupId: Int) {
        deleteContents(groupId)
        insert(items)
    }

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(item: Content)

    @Query("select * from contents where groupID = :groupId and contentKey = :key")
    abstract fun getContent(groupId: Int, key: String): LiveData<Content>

    @Query("select * from contents where groupID = :groupId and contentKey = :key")
    abstract fun getContentAsFlow(groupId: Int, key: String): Flow<Content>

    @Query("delete from contents where groupID = :groupId and contentKey = :key")
    abstract suspend fun deleteContent(groupId: Int, key: String)

    @Query("delete from contents where groupID = :groupId and contentKey = :key")
    abstract fun deleteContentSync(groupId: Int, key: String)

    @Transaction
    open suspend fun cacheContent(item: Content, groupId: Int, key: String) {
        deleteContent(groupId, key)
        insert(item)
    }

}

@Dao
abstract class NotificationsDao {

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(items: List<Notifications>)

    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(item: Notifications)

    @Query("select * from notifications")
    abstract fun getAllNotifications(): Flow<List<Notifications>>

    @Query("select * from notifications")
    abstract fun getAllNotificationsAsPaged(): DataSource.Factory<Int, Notifications>

    @Query("delete from notifications")
    abstract suspend fun deleteNotifications()

    @Query("delete from notifications")
    abstract fun deleteNotificationsSync()

    @Transaction
    open suspend fun cacheNotifications(items: List<Notifications>) {
        deleteNotifications()
        insert(items)
    }

}

@Database(
    entities = [
        Category::class,
        Assocation::class,
        Wallet::class,
        FriendshipStats::class,
        PersonalInfo::class,
        Content::class,
        WeeklyStat::class,
        Notifications::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract val categoriesDao: CategoriesDao
    abstract val assocationsDao: AssocationsDao
    abstract val reportDao: ReportDao
    abstract val usersDao: UsersDao
    abstract val allContentsDao: ContentsDao
    abstract val notificationsDao: NotificationsDao
}

private const val DATABASE_NAME = "LifeSteps.db"
private lateinit var INSTANCE: AppDatabase

fun getDatabase(context: Context): AppDatabase {
    synchronized(AppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}