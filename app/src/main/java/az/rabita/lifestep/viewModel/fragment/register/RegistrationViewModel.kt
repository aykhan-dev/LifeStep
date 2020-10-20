package az.rabita.lifestep.viewModel.fragment.register

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
import az.rabita.lifestep.pojo.apiPOJO.model.CheckEmailModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.RegisterModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import com.onesignal.OneSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private var _eventNavigateToNextRegisterFragment = MutableLiveData<Boolean>()
    val eventNavigateToNextRegisterFragment: LiveData<Boolean> =
        _eventNavigateToNextRegisterFragment

    private var _eventNavigateToMainActivity = MutableLiveData<Boolean>()
    val eventNavigateToMainActivity: LiveData<Boolean> = _eventNavigateToMainActivity

    private val _errorMessage = MutableLiveData<Message>()
    val errorMessage: LiveData<Message> = _errorMessage

    private val _eventExpiredToken = MutableLiveData<Boolean>()
    val eventExpiredToken: LiveData<Boolean> = _eventExpiredToken

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

        if (areValidFirstRegistrationFields()) {

            val model = CheckEmailModelPOJO(
                email = emailInput.value ?: "",
                password = passwordInput.value ?: "",
                passwordConfirm = passwordConfirmInput.value ?: ""
            )

            viewModelScope.launch {

                uiState.value = UiState.Loading

                val lang = sharedPreferences.langCode

                when (val response = usersRepository.checkEmail(lang, model)) {
                    is NetworkResult.Success<*> -> _eventNavigateToNextRegisterFragment.onOff()
                    is NetworkResult.Failure -> when (response.type) {
                        NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                        else -> handleNetworkException(response.message)
                    }
                }

                uiState.value = UiState.LoadingFinished

            }
        }
    }

    fun secondRegistrationPartChecking() {

        val playerId = OneSignal.getPermissionSubscriptionState().subscriptionStatus.userId

        if (!playerId.isNullOrEmpty()) {

            if (areValidSecondRegistrationFields()) {

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

                viewModelScope.launch {

                    uiState.value = UiState.Loading

                    val lang = sharedPreferences.langCode

                    when (val response = usersRepository.registerUser(lang, model)) {
                        is NetworkResult.Success<*> -> {
                            val data = response.data as List<TokenContentPOJO>
                            sharedPreferences.token = data[0].token
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

    private fun areValidFirstRegistrationFields(): Boolean = when {
        (!isEmailValid(emailInput.value ?: "")) -> {
            showMessageDialogSync(getString(R.string.invalid_email), MessageType.ERROR)
            false
        }
        (!isPasswordValid(passwordInput.value ?: "")) -> {
            showMessageDialogSync(getString(R.string.invalid_password), MessageType.ERROR)
            false
        }
        (!isPasswordValid(passwordConfirmInput.value ?: "")) -> {
            showMessageDialogSync(getString(R.string.invalid_password_confirm), MessageType.ERROR)
            false
        }
        (passwordInput.value ?: "" != passwordConfirmInput.value ?: "") -> {
            showMessageDialogSync(getString(R.string.not_same_passwords), MessageType.ERROR)
            false
        }
        else -> true
    }

    private fun areValidSecondRegistrationFields(): Boolean = when {
        (nameInput.value ?: "").isEmpty() -> {
            showMessageDialogSync(getString(R.string.invalid_name), MessageType.ERROR)
            false
        }
        (surnameInput.value ?: "").isEmpty() -> {
            showMessageDialogSync(getString(R.string.invalid_surname), MessageType.ERROR)
            false
        }
        (genderInput.value == null) -> {
            showMessageDialogSync(getString(R.string.invalid_gender), MessageType.ERROR)
            false
        }
        (phoneInput.value ?: "").isEmpty() -> {
            showMessageDialogSync(getString(R.string.invalid_phone), MessageType.ERROR)
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
        sharedPreferences.token = ""
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

    private fun getString(resId: Int): String = context.getString(resId)

}