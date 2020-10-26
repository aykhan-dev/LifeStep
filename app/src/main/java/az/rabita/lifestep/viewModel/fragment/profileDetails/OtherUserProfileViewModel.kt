@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.viewModel.fragment.profileDetails

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.content.PersonalInfoContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendRequestModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.SendStepModelPOJO
import az.rabita.lifestep.pojo.dataHolder.AllInOneOtherUserInfoHolder
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.FriendshipRepository
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.custom.BarDiagram
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.ui.fragment.otherUserProfile.FriendshipStatus
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.extractDiagramData
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import az.rabita.lifestep.utils.notifyObservers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class OtherUserProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val transactionsRepository = TransactionsRepository
    private val friendshipRepository = FriendshipRepository

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    private val _dailyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val dailyStats: LiveData<BarDiagram.DiagramDataModel> = _dailyStats

    private val _monthlyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val monthlyStats: LiveData<BarDiagram.DiagramDataModel> = _monthlyStats

    private val _profileInfo = MutableLiveData<PersonalInfoContentPOJO>()
    val profileInfo: LiveData<PersonalInfoContentPOJO> get() = _profileInfo

    val isDailyStatsShown = MutableLiveData(true)

    val cachedOwnProfileInfo = usersRepository.personalInfo.asLiveData()

    val friendshipStatus: LiveData<FriendshipStatus> = MutableLiveData()

    val uiState = MutableLiveData<UiState>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (uiState.value == UiState.Loading) uiState.value = UiState.LoadingFinished
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun fetchAllInOneProfileInfo(userId: String) {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val response = usersRepository.getUserInfo(token, lang, userId)

            val data = (response as NetworkResult.Success<AllInOneOtherUserInfoHolder>).data
            _profileInfo.postValue(data.info.also {
                (friendshipStatus as MutableLiveData).postValue(
                    when (it.friendShipStatus) {
                        0 -> FriendshipStatus.NOT_FRIEND
                        10 -> FriendshipStatus.PENDING
                        20 -> FriendshipStatus.IS_FRIEND
                        else -> FriendshipStatus.NOT_FRIEND
                    }
                )
            })
            _dailyStats.postValue(extractDiagramData(data.dailyStats))
            _monthlyStats.postValue(extractDiagramData(data.monthlyStats))

            uiState.value = UiState.LoadingFinished

        }

    }

    fun updateOwnProfileData() {

        viewModelScope.launch(exceptionHandler) {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            usersRepository.getPersonalInfo(token, lang)

        }

    }

    fun sendFriendRequest() {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = FriendRequestModelPOJO(friendId = profileInfo.value?.id ?: "")

            friendshipRepository.sendFriendRequest(token, lang, model)
            fetchAllInOneProfileInfo(profileInfo.value?.id ?: "")

            uiState.value = UiState.LoadingFinished

        }

    }

    fun sendStep(amount: Long) {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val date = Calendar.getInstance().time
            val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

            val model = SendStepModelPOJO(
                userId = profileInfo.value?.id ?: "",
                count = amount,
                date = formatted.toString()
            )

            transactionsRepository.transferSteps(token, lang, model)

            profileInfo.value?.let {
                fetchAllInOneProfileInfo(it.id)
            }

            uiState.value = UiState.LoadingFinished

        }

    }

    fun onDailyTextClick() {
        isDailyStatsShown.value = true
        _dailyStats.notifyObservers()
    }

    fun onMonthlyTextClick() {
        isDailyStatsShown.value = false
        _monthlyStats.notifyObservers()
    }

    private fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(
            context.getString(R.string.no_internet_connection),
            MessageType.NO_INTERNET
        )
    }

    private fun showMessageDialog(message: String, type: MessageType) {
        _errorMessage.value = Message(message, type)
        _errorMessage.value = null
    }

    private fun startExpireTokenProcess() {
        sharedPreferences.token = ""
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}