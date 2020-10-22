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
import az.rabita.lifestep.pojo.apiPOJO.content.TokenContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.CheckEmailModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.RegisterModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

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

    val emailInput = MutableLiveData<String?>()
    val passwordInput = MutableLiveData<String>()
    val passwordConfirmInput = MutableLiveData<String>()

    val nameInput = MutableLiveData<String>()
    val surnameInput = MutableLiveData<String>()
    val genderInput = MutableLiveData<Int>()
    val phoneInput = MutableLiveData<String>()
    val invitationCodeInput = MutableLiveData<String>()

    val uiState = MutableLiveData<UiState>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (uiState.value == UiState.Loading) uiState.value = UiState.LoadingFinished
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun firstRegistrationPartChecking() {

        if (areValidFirstRegistrationFields()) {

            val model = CheckEmailModelPOJO(
                email = emailInput.value ?: "",
                password = passwordInput.value ?: "",
                passwordConfirm = passwordConfirmInput.value ?: ""
            )

            viewModelScope.launch(exceptionHandler) {

                uiState.value = UiState.Loading

                val lang = sharedPreferences.langCode

                usersRepository.checkEmail(lang, model)

                _eventNavigateToNextRegisterFragment.onOff()

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

                viewModelScope.launch(exceptionHandler) {

                    uiState.value = UiState.Loading

                    val lang = sharedPreferences.langCode

                    val response = usersRepository.registerUser(lang, model)

                    val data = (response as NetworkResult.Success<List<TokenContentPOJO>>).data
                    sharedPreferences.token = data[0].token
                    _eventNavigateToMainActivity.onOff()

                    uiState.value = UiState.LoadingFinished

                }

            }

        } else showMessageDialog(getString(R.string.device_id_error_message), MessageType.ERROR)
    }

    private fun areValidFirstRegistrationFields(): Boolean = when {
        (!isEmailValid(emailInput.value ?: "")) -> {
            showMessageDialog(getString(R.string.invalid_email), MessageType.ERROR)
            false
        }
        (!isPasswordValid(passwordInput.value ?: "")) -> {
            showMessageDialog(getString(R.string.invalid_password), MessageType.ERROR)
            false
        }
        (!isPasswordValid(passwordConfirmInput.value ?: "")) -> {
            showMessageDialog(getString(R.string.invalid_password_confirm), MessageType.ERROR)
            false
        }
        (passwordInput.value ?: "" != passwordConfirmInput.value ?: "") -> {
            showMessageDialog(getString(R.string.not_same_passwords), MessageType.ERROR)
            false
        }
        else -> true
    }

    private fun areValidSecondRegistrationFields(): Boolean = when {
        (nameInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_name), MessageType.ERROR)
            false
        }
        (surnameInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_surname), MessageType.ERROR)
            false
        }
        (genderInput.value == null) -> {
            showMessageDialog(getString(R.string.invalid_gender), MessageType.ERROR)
            false
        }
        (phoneInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_phone), MessageType.ERROR)
            false
        }
        else -> true
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

    private fun getString(resId: Int): String = context.getString(resId)

}