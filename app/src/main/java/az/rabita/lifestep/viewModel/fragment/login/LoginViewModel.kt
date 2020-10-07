package az.rabita.lifestep.viewModel.fragment.login

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
import az.rabita.lifestep.pojo.apiPOJO.content.TokenContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.LoginModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import com.onesignal.OneSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Suppress("UNCHECKED_CAST")
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private var _stateToRegisterButtonClick = MutableLiveData<Boolean>()
    val stateToRegisterButtonClick: LiveData<Boolean> = _stateToRegisterButtonClick

    private var _eventNavigateToMainActivity = MutableLiveData<Boolean>()
    val eventNavigateToMainActivity: LiveData<Boolean> = _eventNavigateToMainActivity

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message>()
    val errorMessage: LiveData<Message> get() = _errorMessage

    val emailInput = MutableLiveData<String>()
    val passwordInput = MutableLiveData<String>()

    val uiState = MutableLiveData<UiState>()

    fun onToRegisterButtonClick() = _stateToRegisterButtonClick.onOff()

    fun loginUser() {

        val playerId = OneSignal.getPermissionSubscriptionState().subscriptionStatus.userId

        if (!playerId.isNullOrEmpty()) {

            Timber.e(playerId)

            if (validateLoginFields()) {

                val model = LoginModelPOJO(
                    email = emailInput.value ?: "",
                    password = passwordInput.value ?: "",
                    playerId = playerId
                )

                viewModelScope.launch {

                    uiState.value = UiState.Loading

                    val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

                    when (val response = usersRepository.loginUser(lang, model)) {
                        is NetworkResult.Success<*> -> {
                            val data = response.data as List<TokenContentPOJO>
                            sharedPreferences.setStringElement(TOKEN_KEY, data[0].token)
                            _eventNavigateToMainActivity.onOff()
                        }
                        is NetworkResult.Failure -> when (response.type) {
                            NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                            else -> handleNetworkException(response.message)
                        }
                    }

                    uiState.value = UiState.LoadingFinished

                }

            }

        } else showMessageDialogSync(getString(R.string.device_id_error_message), MessageType.ERROR)

    }

    private fun validateLoginFields(): Boolean = when {
        (!isEmailValid(emailInput.value ?: "")) -> {
            showMessageDialogSync(getString(R.string.invalid_email), MessageType.ERROR)
            false
        }
        (!isPasswordValid(passwordInput.value ?: "")) -> {
            showMessageDialogSync(getString(R.string.invalid_password), MessageType.ERROR)
            false
        }
        else -> true
    }

    private suspend fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(
            context.getString(R.string.no_internet_connection),
            MessageType.NO_INTERNET
        )
    }

    private fun showMessageDialogSync(message: String, type: MessageType) {
        _errorMessage.value = Message(message, type)
        _errorMessage.value = null
    }

    private suspend fun showMessageDialog(message: String, type: MessageType): Unit =
        withContext(Dispatchers.Main) {
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

    private fun getString(res: Int) = context.getString(res)

}