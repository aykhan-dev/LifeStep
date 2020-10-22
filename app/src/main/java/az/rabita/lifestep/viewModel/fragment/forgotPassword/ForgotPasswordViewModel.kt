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
import az.rabita.lifestep.pojo.apiPOJO.content.OtpContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.EmailModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.ForgotPasswordModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.RefreshPasswordModelPOJO
import az.rabita.lifestep.pojo.dataHolder.ForgotPasswordInfoHolder
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

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

    private var _eventNavigateBackToEmailFragment = MutableLiveData<Boolean>()
    val eventNavigateBackToEmailFragment: LiveData<Boolean> = _eventNavigateBackToEmailFragment

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

    val passwordInput = MutableLiveData<String?>()

    val passwordConfirmInput = MutableLiveData<String>()

    private var details: ForgotPasswordInfoHolder? = null

    val uiState = MutableLiveData<UiState>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (uiState.value == UiState.Loading) uiState.value = UiState.LoadingFinished
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    var pinGeneratedTime = 0L

    fun onEmailConfirmClick() = sendEmailRequest(emailInput.value ?: "")

    fun onPinConfirmClick() {
        //if(System.currentTimeMillis() - pinGeneratedTime > 30000) _eventNavigateBackToEmailFragment.onOff()
        viewModelScope.launch {
            if (checkPinCode(pinInput.value ?: "")) _eventNavigateToPasswordFragment.onOff()
            else showMessageDialog(getString(R.string.invalid_pin_code), MessageType.ERROR)
        }
    }

    fun onPasswordConfirmClick() {

        viewModelScope.launch(exceptionHandler) {
            if (checkPasswords()) {
                if (fromEditProfilePage) sendUpdatePasswordRequest()
                else sendChangePasswordRequest()
            } else showMessageDialog(getString(R.string.not_same_passwords), MessageType.ERROR)
        }
    }

    private fun sendEmailRequest(email: String) {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val model = EmailModelPOJO(email)

            val lang = sharedPreferences.langCode

            val response = usersRepository.sendOtp(lang, model)

            val data = (response as NetworkResult.Success<List<OtpContentPOJO>>).data
            details = ForgotPasswordInfoHolder(data[0].userId, data[0].otp)
            _eventNavigateToPinFragment.onOff()

            uiState.value = UiState.LoadingFinished

        }

    }

    private suspend fun sendChangePasswordRequest() {

        uiState.value = UiState.Loading

        val model = ForgotPasswordModelPOJO(
            id = details?.id ?: "",
            otp = details?.otp ?: "",
            password = passwordInput.value ?: "",
            confirmPassword = passwordConfirmInput.value ?: ""
        )

        val lang = sharedPreferences.langCode

        val response = usersRepository.changePassword(lang, model)

        val successData = (response as NetworkResult.Success<*>).data
        successData?.let { _eventBack.onOff() }

        uiState.value = UiState.LoadingFinished

    }

    private suspend fun sendUpdatePasswordRequest() {

        uiState.value = UiState.Loading

        val token = sharedPreferences.token
        val lang = sharedPreferences.langCode

        val model = RefreshPasswordModelPOJO(
            password = passwordInput.value ?: "",
            confirmPassword = passwordConfirmInput.value ?: ""
        )

        val response = usersRepository.updatePassword(token, lang, model)

        val successData = (response as NetworkResult.Success<*>).data
        successData?.let { _eventBack.onOff() }

        uiState.value = UiState.LoadingFinished

    }

    private fun checkPasswords(): Boolean {
        return passwordInput.value ?: "" == passwordConfirmInput.value ?: ""
    }

    private fun checkPinCode(pin: String): Boolean = (pin == details?.otp)

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