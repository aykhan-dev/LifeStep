package az.rabita.lifestep.viewModel.fragment.sendStep

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.model.SendStepModelPOJO
import az.rabita.lifestep.pojo.dataHolder.UserProfileInfoHolder
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SendStepViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val transactionsRepository = TransactionsRepository

    lateinit var personalInfo: UserProfileInfoHolder

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventDismissDialog = MutableLiveData<Boolean>()
    val eventDismissDialog: LiveData<Boolean> get() = _eventDismissDialog

    val sendStepInput = MutableLiveData<String>().apply {
        observeForever {
            if (::personalInfo.isInitialized) {
                val input = if (value?.isEmpty() != false) 0 else (value ?: "0").toLong()
                if (input > personalInfo.balance) postValue("${personalInfo.balance}")
            }
        }
    }

    fun sendStep(userId: String) {

        viewModelScope.launch {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val date = Calendar.getInstance().time
            val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

            val model = SendStepModelPOJO(
                userId = userId,
                count = (sendStepInput.value ?: "0").toLong(),
                date = formatted.toString()
            )

            when (val response = transactionsRepository.transferSteps(token, lang, model)) {
                is NetworkResult.Success<*> -> _eventDismissDialog.onOff()
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

    private suspend fun startExpireTokenProcess(): Unit = withContext(Dispatchers.Main) {
        sharedPreferences.token = ""
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }


}