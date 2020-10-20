package az.rabita.lifestep.viewModel.fragment.settings

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.*

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val reportRepository = ReportRepository.getInstance(getDatabase(context))

    val friendshipStats = reportRepository.friendshipStats.asLiveData()

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventLogOut = MutableLiveData(false)
    val eventLogOut: LiveData<Boolean> = _eventLogOut

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    fun fetchFriendshipStats() {

        viewModelScope.launch {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            when (val response = reportRepository.getFriendshipStats(token, lang)) {
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    fun logOut() {

        viewModelScope.launch {

            val db = getDatabase(context)

            val tasks = listOf(
                async { db.usersDao.deletePersonalInfo() },
                async { db.reportDao.deleteAllWeeklyStats() },
                async { db.reportDao.deleteAllFriendshipStats() },
                async { db.reportDao.deleteWalletInfo() },
                async { db.notificationsDao.deleteNotifications() },
                async { db.allContentsDao.deleteContent(INVITE_FRIENDS_GROUP_ID, INVITE_TEXT_KEY) }
            )

            tasks.awaitAll()

            if (_eventLogOut.value == false) _eventLogOut.postValue(true)

        }

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
        sharedPreferences.token = ""
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

    fun endLogOut() {
        _eventLogOut.value = false
    }

}