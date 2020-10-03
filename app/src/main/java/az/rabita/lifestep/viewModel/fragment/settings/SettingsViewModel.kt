package az.rabita.lifestep.viewModel.fragment.settings

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val reportRepository = ReportRepository.getInstance(getDatabase(context))

    val friendshipStats = reportRepository.friendshipStats.asLiveData()

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventLogOut = MutableLiveData<Boolean>().apply { value = false }
    val eventLogOut: LiveData<Boolean> = _eventLogOut

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchFriendshipStats() {
        viewModelScope.launch(Dispatchers.IO) {

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
        viewModelScope.launch(Dispatchers.IO) {
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

    private suspend fun handleNetworkException(exception: String?) {
            if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
            else showMessageDialog(context.getString(R.string.no_internet_connection))
        }

    private suspend fun showMessageDialog(message: String?): Unit = withContext(Dispatchers.Main) {
        _errorMessage.value = message
        _errorMessage.value = null
    }

    private suspend fun startExpireTokenProcess(): Unit = withContext(Dispatchers.Main) {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

    fun endLogOut() {
        _eventLogOut.value = false
    }

}