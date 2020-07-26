package az.rabita.lifestep.viewModel.fragment.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.TokenContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.LoginModelPOJO
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.*
import com.onesignal.OneSignal
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("UNCHECKED_CAST")
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private val _stateInternetConnection = MutableLiveData<Boolean>()
    val stateInternetConnection: LiveData<Boolean> get() = _stateInternetConnection

    private var _stateToRegisterButtonClick = MutableLiveData<Boolean>()
    val stateToRegisterButtonClick: LiveData<Boolean> get() = _stateToRegisterButtonClick

    private var _eventNavigateToMainActivity = MutableLiveData<Boolean>()
    val eventNavigateToMainActivity: LiveData<Boolean> get() = _eventNavigateToMainActivity

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    val emailInput = MutableLiveData<String>()
    val passwordInput = MutableLiveData<String>()

    val uiState = MutableLiveData<UiState>()

    fun onToRegisterButtonClick() = _stateToRegisterButtonClick.onOff()

    fun loginUser() {
        viewModelScope.launch {
            val playerId = OneSignal.getPermissionSubscriptionState().subscriptionStatus.userId

            if (!playerId.isNullOrEmpty()) {
                Timber.e(playerId)

                uiState.value = (UiState.Loading)

                if (validateLoginFields()) {
                    val model = LoginModelPOJO(
                        email = emailInput.value ?: "",
                        password = passwordInput.value ?: "",
                        playerId = playerId
                    )

                    val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

                    when (val response = usersRepository.loginUser(lang, model)) {
                        is NetworkState.Success<*> -> {
                            val data = response.data as List<TokenContentPOJO>
                            sharedPreferences.setStringElement(TOKEN_KEY, data[0].token)
                            _eventNavigateToMainActivity.onOff()
                        }
                        is NetworkState.ExpiredToken -> startExpireTokenProcess()
                        is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                        is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                        is NetworkState.NetworkException -> handleNetworkException(response.exception)
                    }
                }

                uiState.value = (UiState.LoadingFinished)

            } else showMessageDialog(getString(R.string.device_id_error_message))

        }
    }

    private fun validateLoginFields(): Boolean = when {
        (!isEmailValid(emailInput.value ?: "")) -> {
            showMessageDialog(getString(R.string.invalid_email))
            false
        }
        (!isPasswordValid(passwordInput.value ?: "")) -> {
            showMessageDialog(getString(R.string.invalid_password))
            false
        }
        else -> true
    }

    private fun handleNetworkException(exception: String?) {
        viewModelScope.launch {
            if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
            else showMessageDialog(NO_INTERNET_CONNECTION)
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

    private fun getString(res: Int) = context.getString(res)

}