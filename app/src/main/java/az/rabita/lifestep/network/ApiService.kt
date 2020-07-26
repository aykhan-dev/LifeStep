package az.rabita.lifestep.network

import az.rabita.lifestep.BuildConfig
import az.rabita.lifestep.pojo.apiPOJO.ServerResponsePOJO
import az.rabita.lifestep.pojo.apiPOJO.content.*
import az.rabita.lifestep.pojo.apiPOJO.model.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://demo20.rabita.az/api/v1/"

interface UsersService {

    @POST("users/login")
    suspend fun loginUser(
        @Header("lang") lang: Int,
        @Header("token") token: String,
        @Body model: LoginModelPOJO
    ): Response<ServerResponsePOJO<TokenContentPOJO>>

    @POST("users/register")
    suspend fun registerUser(
        @Header("lang") lang: Int,
        @Header("token") token: String,
        @Body model: RegisterModelPOJO
    ): Response<ServerResponsePOJO<TokenContentPOJO>>

    @POST("users/register/checkEmail")
    suspend fun checkEmail(
        @Header("lang") lang: Int,
        @Header("token") token: String,
        @Body model: CheckEmailModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("users/sendotp")
    suspend fun sendOtp(
        @Header("lang") lang: Int,
        @Header("token") token: String,
        @Body model: EmailModelPOJO
    ): Response<ServerResponsePOJO<OtpContentPOJO>>

    @POST("users/forgotpassword")
    suspend fun changePassword(
        @Header("lang") lang: Int,
        @Header("token") token: String,
        @Body model: ForgotPasswordModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("users/updatepassword")
    suspend fun updatePassword(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: RefreshPasswordModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("users/donations")
    suspend fun getDonors(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: DonorsModelPOJO
    ): Response<ServerResponsePOJO<RankerContentPOJO>>

    @GET("users/info")
    suspend fun getUserInfo(
        @Header("token") token: String,
        @Header("lang") lang: Int
    ): Response<ServerResponsePOJO<OwnProfileInfoContentPOJO>>

    @GET("users/infobyid")
    suspend fun getUserInfoById(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("usersId") userId: String
    ): Response<ServerResponsePOJO<PersonalInfoContentPOJO>>

    @POST("users/edit")
    suspend fun saveProfileDetailsChanges(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: ChangedProfileDetailsModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @Multipart
    @POST("users/changePicture")
    suspend fun changeProfilePicture(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Part imageFile: MultipartBody.Part
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @GET("users/searchbyfullname")
    suspend fun searchByFullName(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("fullname") fullName: String
    ): Response<ServerResponsePOJO<SearchResultContentPOJO>>

}

interface FriendshipService {

    @GET("friendships/list")
    suspend fun getFriendsList(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("pagenumber") page: Int
    ): Response<ServerResponsePOJO<FriendContentPOJO>>

    @GET("friendships/pending")
    suspend fun getFriendRequestList(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("pagenumber") page: Int
    ): Response<ServerResponsePOJO<FriendContentPOJO>>

    @POST("friendships/approve")
    suspend fun acceptFriendRequest(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body friendshipActionModelPOJO: FriendshipActionModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("friendships/reject")
    suspend fun rejectFriendRequest(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body friendshipActionModelPOJO: FriendshipActionModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("friendships/send")
    suspend fun sendFriendRequest(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: FriendRequestModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

}

interface AdvertisementsService {

    @POST("advetisements/createtransaction")
    suspend fun createAdsTransaction(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: DateModelPOJO
    ): Response<ServerResponsePOJO<AdsTransactionContentPOJO>>

    @POST("advetisements/convertsteps")
    suspend fun sendAdsTransactionResult(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: ConvertStepsModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("advetisements/bonussteps")
    suspend fun sendBonusAdsTransactionResult(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: ConvertStepsModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

}

interface ContentsService {

    @POST("contents/bypageid")
    suspend fun getContent(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("translationsGroups") groupID: Int
    ): Response<ServerResponsePOJO<ContentContentPOJO>>

}

interface ReportService {

    @POST("report/transactiontotal")
    suspend fun getTotalTransactionInfo(
        @Header("token") token: String,
        @Header("lang") lang: Int
    ): Response<ServerResponsePOJO<WalletContentPOJO>>

    @GET("report/friendships")
    suspend fun getFriendshipStats(
        @Header("token") token: String,
        @Header("lang") lang: Int
    ): Response<ServerResponsePOJO<FriendshipContentPOJO>>

    @GET("report/daily")
    suspend fun getDailyStats(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("usersId") userId: String
    ): Response<ServerResponsePOJO<DailyContentPOJO>>

    @GET("report/monthly")
    suspend fun getMonthlyStats(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("usersId") userId: String
    ): Response<ServerResponsePOJO<MonthlyContentPOJO>>

    @GET("report/championsofweek")
    suspend fun getChampionsOfWeek(
        @Header("token") token: String,
        @Header("lang") lang: Int
    ): Response<ServerResponsePOJO<RankerContentPOJO>>

    @GET("report/weekly")
    suspend fun getWeeklyStats(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("dateOfToday") date: String
    ): Response<ServerResponsePOJO<WeeklyContentPOJO>>

}

interface NotificationsService {

    @POST("notifications/list")
    suspend fun fetchNotifications(
        @Header("token") token: String,
        @Header("lang") lang: Int
    ): Response<ServerResponsePOJO<NotificationContentPOJO>>

}

interface TransactionsService {

    @POST("transaction/donationstep")
    suspend fun donateStep(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: DonateStepModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @GET("transaction/history/donations")
    suspend fun fetchDonations(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("pageNumber") page: Int
    ): Response<ServerResponsePOJO<HistoryItemContentPOJO>>

    @GET("transaction/history/transfer")
    suspend fun fetchTransfers(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("pageNumber") page: Int
    ): Response<ServerResponsePOJO<HistoryItemContentPOJO>>

    @GET("transaction/history/bonus")
    suspend fun fetchBonus(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("pageNumber") page: Int
    ): Response<ServerResponsePOJO<HistoryItemContentPOJO>>

    @POST("transaction/transferstep")
    suspend fun sendStep(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: SendStepModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("transaction/currentstep")
    suspend fun sendCurrentStepCount(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: CurrentStepModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("transaction/convertstep")
    suspend fun convertSteps(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: DateModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

    @POST("transaction/bonusstep")
    suspend fun getBonusSteps(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Body model: DateModelPOJO
    ): Response<ServerResponsePOJO<EmptyContentPOJO>>

}

interface CategoriesService {

    @GET("categories/all")
    suspend fun getAllCategories(
        @Header("token") token: String,
        @Header("lang") lang: Int
    ): Response<ServerResponsePOJO<CategoriesContentPOJO>>

}

interface AssocationsService {

    @GET("assocations/all")
    suspend fun getAllAssocations(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("categoriesId") id: Int
    ): Response<ServerResponsePOJO<AssocationContentPOJO>>

    @GET("assocations/details")
    suspend fun getDetailsOfAssocation(
        @Header("token") token: String,
        @Header("lang") lang: Int,
        @Query("assocationsId") id: String
    ): Response<ServerResponsePOJO<AssocationDetailsContentPOJO>>

}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object ApiInitHelper {

    private val okHttpClientBuilder = OkHttpClient.Builder()
    private var retrofit: Retrofit? = null

    private fun okHttpClient(): OkHttpClient {
        when {
            BuildConfig.DEBUG -> {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                okHttpClientBuilder.addInterceptor(logging)
            }
        }
        return okHttpClientBuilder.build()
    }

    private fun getClient(): Retrofit {
        when (retrofit) {
            null -> {
                retrofit = Retrofit.Builder()
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .client(okHttpClient())
                    .baseUrl(BASE_URL)
                    .build()
            }
        }
        return retrofit as Retrofit
    }

    val usersService: UsersService by lazy { getClient().create(UsersService::class.java) }
    val friendshipService: FriendshipService by lazy { getClient().create(FriendshipService::class.java) }
    val contentsService: ContentsService by lazy { getClient().create(ContentsService::class.java) }
    val categoriesService: CategoriesService by lazy { getClient().create(CategoriesService::class.java) }
    val assocationsService: AssocationsService by lazy { getClient().create(AssocationsService::class.java) }
    val transactionsService: TransactionsService by lazy { getClient().create(TransactionsService::class.java) }
    val notificationsService: NotificationsService by lazy { getClient().create(NotificationsService::class.java) }
    val reportService: ReportService by lazy { getClient().create(ReportService::class.java) }
    val advertisementsService: AdvertisementsService by lazy {
        getClient().create(
            AdvertisementsService::class.java
        )
    }
}