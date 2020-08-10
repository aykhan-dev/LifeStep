package az.rabita.lifestep.viewModel.fragment.settings

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val scope = CoroutineScope(IO)

    private val reportRepository = ReportRepository.getInstance(getDatabase(context))

    val friendshipStats = reportRepository.friendshipStats.asLiveData()

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventLogOut = MutableLiveData<Boolean>().apply { value = false }
    val eventLogOut: LiveData<Boolean> = _eventLogOut

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchFriendshipStats() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getFriendshipStats(token, lang)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    fun logOut() {
        scope.launch {
            with(getDatabase(context)) {
                usersDao.deletePersonalInfoSync()
                with(reportDao) {
                    deleteAllWeeklyStatsSync()
                    deleteAllFriendshipStatsSync()
                    deleteWalletInfoSync()
                }
                notificationsDao.deleteNotificationsSync()
                allContentsDao.deleteContentSync(INVITE_FRIENDS_GROUP_ID, INVITE_TEXT_KEY)
                _eventLogOut.postValue(true)
            }
        }
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

    fun endLogOut() {
        _eventLogOut.value = false
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

}