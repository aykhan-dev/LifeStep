package az.rabita.lifestep.viewModel.fragment.forgotPassword

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
import az.rabita.lifestep.pojo.apiPOJO.content.OtpContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.EmailModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.ForgotPasswordModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.RefreshPasswordModelPOJO
import az.rabita.lifestep.pojo.dataHolder.ForgotPasswordInfoHolder
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences by lazy { PreferenceManager.getInstance(context) }

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private var _fromEditProfile = false
    val fromEditProfilePage: Boolean get() = _fromEditProfile

    private var _stateEmailConfirmButtonEnable = MutableLiveData<Boolean>()
    val stateEmailConfirmButtonEnable: LiveData<Boolean> = _stateEmailConfirmButtonEnable

    private var _statePinConfirmButtonEnable = MutableLiveData<Boolean>()
    val statePinConfirmButtonEnable: LiveData<Boolean> = _statePinConfirmButtonEnable

    private var _eventNavigateToPinFragment = MutableLiveData<Boolean>()
    val eventNavigateToPinFragment: LiveData<Boolean> = _eventNavigateToPinFragment

    private var _eventNavigateToPasswordFragment = MutableLiveData<Boolean>()
    val eventNavigateToPasswordFragment: LiveData<Boolean> = _eventNavigateToPasswordFragment

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _eventBack = MutableLiveData<Boolean>()
    val eventBack: LiveData<Boolean> = _eventBack

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    val emailInput = MutableLiveData<String>().apply {
        observeForever { _stateEmailConfirmButtonEnable.value = isEmailValid(it) }
    }
    val pinInput = MutableLiveData<String>().apply {
        observeForever { _statePinConfirmButtonEnable.value = isPinValid(it) }
    }

    val passwordInput = MutableLiveData<String>()

    val passwordConfirmInput = MutableLiveData<String>()

    private var details: ForgotPasswordInfoHolder? = null

    val uiState = MutableLiveData<UiState>()

    fun onEmailConfirmClick() = sendEmailRequest(emailInput.value ?: "")

    fun onPinConfirmClick() {
        viewModelScope.launch {
            if (checkPinCode(pinInput.value ?: "")) _eventNavigateToPasswordFragment.onOff()
            else showMessageDialog(getString(R.string.invalid_pin_code), MessageType.ERROR)
        }
    }

    fun onPasswordConfirmClick() {
        viewModelScope.launch {
            if (checkPasswords()) {
                if (fromEditProfilePage) sendUpdatePasswordRequest() else sendChangePasswordRequest()
            } else showMessageDialog(getString(R.string.not_same_passwords), MessageType.ERROR)
        }
    }

    private fun sendEmailRequest(email: String) {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val model = EmailModelPOJO(email)

            val lang = sharedPreferences.langCode

            when (val response = usersRepository.sendOtp(lang, model)) {
                is NetworkResult.Success<*> -> {
                    val data = response.data as List<OtpContentPOJO>
                    details = ForgotPasswordInfoHolder(data[0].userId, data[0].otp)
                    _eventNavigateToPinFragment.onOff()
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }

    }

    private fun sendChangePasswordRequest() {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val model = ForgotPasswordModelPOJO(
                id = details?.id ?: "",
                otp = details?.otp ?: "",
                password = passwordInput.value ?: "",
                confirmPassword = passwordConfirmInput.value ?: ""
            )

            val lang = sharedPreferences.langCode

            when (val response = usersRepository.changePassword(lang, model)) {
                is NetworkResult.Success<*> -> {
                    val successData = response.data
                    successData?.let { _eventBack.onOff() }
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }

    }

    private fun sendUpdatePasswordRequest() {
        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = RefreshPasswordModelPOJO(
                password = passwordInput.value ?: "",
                confirmPassword = passwordConfirmInput.value ?: ""
            )

            when (val response = usersRepository.updatePassword(token, lang, model)) {
                is NetworkResult.Success<*> -> {
                    val successData = response.data
                    successData?.let { _eventBack.onOff() }
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }
    }

    private fun checkPasswords(): Boolean {
        return passwordInput.value ?: "" == passwordConfirmInput.value ?: ""
    }

    private fun checkPinCode(pin: String): Boolean = (pin == details?.otp)

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

    private fun getString(res: Int) = context.getString(res)

    fun changeFromEditProfilePage(booleanExtra: Boolean) {
        _fromEditProfile = booleanExtra
    }

    override fun onCleared() {
        super.onCleared()
        emailInput.removeObserver { }
        pinInput.removeObserver { }
    }

}