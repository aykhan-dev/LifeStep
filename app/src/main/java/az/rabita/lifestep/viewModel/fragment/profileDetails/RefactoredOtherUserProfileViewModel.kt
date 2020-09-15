package az.rabita.lifestep.viewModel.fragment.profileDetails

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.PersonalInfoContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendRequestModelPOJO
import az.rabita.lifestep.pojo.dataHolder.AllInOneOtherUserInfoHolder
import az.rabita.lifestep.repository.FriendshipRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.custom.BarDiagram
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch

class RefactoredOtherUserProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val friendshipRepository = FriendshipRepository.getInstance(
        PaginationListeners(
            {},
            {},
            { handleNetworkException(it) }
        )
    )

    private val _eventExpiredToken = MutableLiveData<Boolean>(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _dailyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val dailyStats: LiveData<BarDiagram.DiagramDataModel> = _dailyStats

    private val _monthlyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val monthlyStats: LiveData<BarDiagram.DiagramDataModel> = _monthlyStats

    private val _profileInfo = MutableLiveData<PersonalInfoContentPOJO>()
    val profileInfo: LiveData<PersonalInfoContentPOJO> get() = _profileInfo

    val isDailyStatsShown = MutableLiveData<Boolean>(true)

    val cachedOwnProfileInfo = usersRepository.personalInfo.asLiveData()

    fun fetchAllInOneProfileInfo(userId: String) {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.getUserInfoAllInOneById(token, lang, userId)) {
                is NetworkState.Success<*> -> run {
                    val data = response.data as AllInOneOtherUserInfoHolder
                    _profileInfo.postValue(data.info)
                    _dailyStats.postValue(extractDiagramData(data.dailyStats))
                    _monthlyStats.postValue(extractDiagramData(data.monthlyStats))
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    fun sendFriendRequest() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)
            val model = FriendRequestModelPOJO(friendId = profileInfo.value?.id ?: "")

            when (val response = friendshipRepository.sendFriendRequest(token, lang, model)) {
                is NetworkState.Success<*> -> fetchAllInOneProfileInfo(profileInfo.value?.id ?: "")
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
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

    private fun handleNetworkException(exception: String?) {
        viewModelScope.launch {
            if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
            else showMessageDialog(context.getString(R.string.no_internet_connection))
        }
    }

    private fun showMessageDialog(message: String?) {
        _errorMessage.value = message
        _errorMessage.value = null
    }

    private fun startExpireTokenProcess() {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}