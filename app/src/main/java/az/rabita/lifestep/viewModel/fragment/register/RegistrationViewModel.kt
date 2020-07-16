package az.rabita.lifestep.viewModel.fragment.register

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
import az.rabita.lifestep.pojo.apiPOJO.model.CheckEmailModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.RegisterModelPOJO
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.*
import com.onesignal.OneSignal
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private var _eventNavigateToNextRegisterFragment = MutableLiveData<Boolean>()
    val eventNavigateToNextRegisterFragment: LiveData<Boolean> get() = _eventNavigateToNextRegisterFragment

    private var _eventNavigateToMainActivity = MutableLiveData<Boolean>()
    val eventNavigateToMainActivity: LiveData<Boolean> get() = _eventNavigateToMainActivity

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _expireToken = MutableLiveData<Boolean>()
    val expireToken: LiveData<Boolean> get() = _expireToken

    val emailInput = MutableLiveData<String>()
    val passwordInput = MutableLiveData<String>()
    val passwordConfirmInput = MutableLiveData<String>()

    val nameInput = MutableLiveData<String>()
    val surnameInput = MutableLiveData<String>()
    val genderInput = MutableLiveData<Int>()
    val phoneInput = MutableLiveData<String>()
    val invitationCodeInput = MutableLiveData<String>()

    val uiState = MutableLiveData<UiState>()

    fun firstRegistrationPartChecking() {
        viewModelScope.launch {

            if (areValidFirstRegistrationFields()) {

                uiState.postValue(UiState.Loading)

                val model = CheckEmailModelPOJO(
                    email = emailInput.value ?: "",
                    password = passwordInput.value ?: "",
                    passwordConfirm = passwordConfirmInput.value ?: ""
                )

                val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

                when (val response = usersRepository.checkEmail(lang, model)) {
                    is NetworkState.Success<*> -> _eventNavigateToNextRegisterFragment.onOff()
                    is NetworkState.ExpiredToken -> startExpireTokenProcess()
                    is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                    is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                    is NetworkState.NetworkException -> showMessageDialog(response.exception)
                }

                uiState.postValue(UiState.LoadingFinished)

            }
        }
    }

    fun secondRegistrationPartChecking() {
        viewModelScope.launch {

            val playerId = OneSignal.getPermissionSubscriptionState().subscriptionStatus.userId

            if (!playerId.isNullOrEmpty()) {
                if (areValidSecondRegistrationFields()) {

                    uiState.postValue(UiState.Loading)

                    val model = RegisterModelPOJO(
                        gender = genderInput.value ?: 0,
                        email = emailInput.value ?: "",
                        playerId = playerId,
                        name = nameInput.value ?: "",
                        surname = surnameInput.value ?: "",
                        invitationCode = invitationCodeInput.value ?: "",
                        phone = phoneInput.value ?: "",
                        password = passwordInput.value ?: "",
                        repeatPassword = passwordConfirmInput.value ?: ""
                    )

                    val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

                    when (val response = usersRepository.registerUser(lang, model)) {
                        is NetworkState.Success<*> -> {
                            val data = response.data as List<TokenContentPOJO>
                            sharedPreferences.setStringElement(TOKEN_KEY, data[0].token)
                            _eventNavigateToMainActivity.onOff()
                        }
                        is NetworkState.ExpiredToken -> startExpireTokenProcess()
                        is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                        is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                        is NetworkState.NetworkException -> showMessageDialog(response.exception)
                    }

                    uiState.postValue(UiState.LoadingFinished)

                }
            } else {
                showMessageDialog(getString(R.string.device_id_error_message))
            }
        }
    }

    private fun areValidFirstRegistrationFields(): Boolean = when {
        (!isEmailValid(emailInput.value ?: "")) -> {
            showMessageDialog(getString(R.string.invalid_email))
            false
        }
        (!isPasswordValid(passwordInput.value ?: "")) -> {
            showMessageDialog(getString(R.string.invalid_password))
            false
        }
        (!isPasswordValid(passwordConfirmInput.value ?: "")) -> {
            showMessageDialog(getString(R.string.invalid_password_confirm))
            false
        }
        (passwordInput.value ?: "" != passwordConfirmInput.value ?: "") -> {
            showMessageDialog(getString(R.string.not_same_passwords))
            false
        }
        else -> true
    }

    private fun areValidSecondRegistrationFields(): Boolean = when {
        (nameInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_name))
            false
        }
        (surnameInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_surname))
            false
        }
        (genderInput.value == null) -> {
            showMessageDialog(getString(R.string.invalid_gender))
            false
        }
        (phoneInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_phone))
            false
        }
        else -> true
    }

    private fun showMessageDialog(message: String?) {
        _errorMessage.value = message
        _errorMessage.value = null
    }

    private fun startExpireTokenProcess() {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_expireToken.value == false) _expireToken.value = true
    }

    fun endExpireTokenProcess() {
        _expireToken.value = false
    }

    private fun getString(resId: Int): String = context.getString(resId)

}