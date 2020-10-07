package az.rabita.lifestep.viewModel.fragment.profileDetails

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.content.PersonalInfoContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendRequestModelPOJO
import az.rabita.lifestep.pojo.dataHolder.AllInOneOtherUserInfoHolder
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.FriendshipRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.custom.BarDiagram
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.ui.fragment.otherUserProfile.FriendshipStatus
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtherUserProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val friendshipRepository = FriendshipRepository

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message>()
    val errorMessage: LiveData<Message> get() = _errorMessage

    private val _dailyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val dailyStats: LiveData<BarDiagram.DiagramDataModel> = _dailyStats

    private val _monthlyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val monthlyStats: LiveData<BarDiagram.DiagramDataModel> = _monthlyStats

    private val _profileInfo = MutableLiveData<PersonalInfoContentPOJO>()
    val profileInfo: LiveData<PersonalInfoContentPOJO> get() = _profileInfo

    val isDailyStatsShown = MutableLiveData<Boolean>(true)

    val cachedOwnProfileInfo = usersRepository.personalInfo.asLiveData()

    val friendshipStatus: LiveData<FriendshipStatus> = MutableLiveData()

    fun fetchAllInOneProfileInfo(userId: String) {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.getUserInfoAllInOneById(token, lang, userId)) {
                is NetworkResult.Success<*> -> run {
                    val data = response.data as AllInOneOtherUserInfoHolder
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
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    fun updateOwnProfileData() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.getPersonalInfo(token, lang)) {
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    fun sendFriendRequest() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val model = FriendRequestModelPOJO(friendId = profileInfo.value?.id ?: "")

            when (val response = friendshipRepository.sendFriendRequest(token, lang, model)) {
                is NetworkResult.Success<*> -> fetchAllInOneProfileInfo(profileInfo.value?.id ?: "")
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

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

    private suspend fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(context.getString(R.string.no_internet_connection), MessageType.NO_INTERNET)
    }

    private suspend fun showMessageDialog(message: String, type: MessageType): Unit = withContext(Dispatchers.Main) {
        _errorMessage.value = Message(message, type)
        _errorMessage.value = null
    }

    private suspend fun startExpireTokenProcess(): Unit = withContext(Dispatchers.Main) {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}