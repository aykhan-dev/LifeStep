package az.rabita.lifestep.viewModel.fragment.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.NotificationsRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.LANG_AZ
import az.rabita.lifestep.utils.LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val notificationsRepository = NotificationsRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message>()
    val errorMessage: LiveData<Message> get() = _errorMessage

    val listOfNotifications = notificationsRepository.listOfNotifications

    fun fetchListOfNotifications() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, LANG_AZ)

            when (val response = notificationsRepository.fetchNotifications(token, lang)) {
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

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

    private suspend fun startExpireTokenProcess(): Unit = withContext(Dispatchers.IO) {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_eventExpiredToken.value == false) _eventExpiredToken.postValue(true)
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}