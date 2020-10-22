package az.rabita.lifestep.viewModel.fragment.settings

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.INVITE_FRIENDS_GROUP_ID
import az.rabita.lifestep.utils.INVITE_TEXT_KEY
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber

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

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun fetchFriendshipStats() {

        viewModelScope.launch(exceptionHandler) {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            reportRepository.getFriendshipStats(token, lang)

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

    fun endLogOut() {
        _eventLogOut.value = false
    }

}