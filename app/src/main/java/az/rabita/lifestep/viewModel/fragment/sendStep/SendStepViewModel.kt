package az.rabita.lifestep.viewModel.fragment.sendStep

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.PersonalInfoContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.SendStepModelPOJO
import az.rabita.lifestep.pojo.dataHolder.UserProfileInfoHolder
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch
import java.util.*

class SendStepViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val transactionsRepository = TransactionsRepository

    lateinit var personalInfo: UserProfileInfoHolder

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
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

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val date = Calendar.getInstance().time
            val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

            val model = SendStepModelPOJO(
                userId = userId,
                count = (sendStepInput.value ?: "0").toLong(),
                date = formatted.toString()
            )

            when (val response = transactionsRepository.transferSteps(token, lang, model)) {
                is NetworkState.Success<*> -> _eventDismissDialog.onOff()
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
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


}