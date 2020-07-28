package az.rabita.lifestep.viewModel.fragment.forgotPassword

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.OtpContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.EmailModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.ForgotPasswordModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.RefreshPasswordModelPOJO
import az.rabita.lifestep.pojo.dataHolder.ForgotPasswordInfoHolder
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.*
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

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _eventBack = MutableLiveData<Boolean>()
    val eventBack: LiveData<Boolean> = _eventBack

    private var _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

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
        if (checkPinCode(pinInput.value ?: "")) _eventNavigateToPasswordFragment.onOff()
        else showMessageDialog(getString(R.string.invalid_pin_code))
    }

    fun onPasswordConfirmClick() {
        if (checkPasswords()) {
            if (fromEditProfilePage) sendUpdatePasswordRequest() else sendChangePasswordRequest()
        } else showMessageDialog(getString(R.string.not_same_passwords))
    }

    private fun sendEmailRequest(email: String) {
        viewModelScope.launch {

            uiState.postValue(UiState.Loading)

            val model = EmailModelPOJO(email)

            when (val response = usersRepository.sendOtp(10, model)) {
                is NetworkState.Success<*> -> {
                    val data = response.data as List<OtpContentPOJO>
                    details = ForgotPasswordInfoHolder(data[0].userId, data[0].otp)
                    _eventNavigateToPinFragment.onOff()
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)

        }
    }

    private fun sendChangePasswordRequest() {
        viewModelScope.launch {

            uiState.postValue(UiState.Loading)

            val model = ForgotPasswordModelPOJO(
                id = details?.id ?: "",
                otp = details?.otp ?: "",
                password = passwordInput.value ?: "",
                confirmPassword = passwordConfirmInput.value ?: ""
            )

            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.changePassword(lang, model)) {
                is NetworkState.Success<*> -> {
                    val successData = response.data
                    successData?.let { _eventBack.onOff() }
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)

        }
    }

    private fun sendUpdatePasswordRequest() {
        viewModelScope.launch {

            uiState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val model = RefreshPasswordModelPOJO(
                password = passwordInput.value ?: "",
                confirmPassword = passwordConfirmInput.value ?: ""
            )

            when (val response = usersRepository.updatePassword(token, lang, model)) {
                is NetworkState.Success<*> -> {
                    val successData = response.data
                    successData?.let { _eventBack.onOff() }
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)

        }
    }

    private fun checkPasswords(): Boolean {
        return passwordInput.value ?: "" == passwordConfirmInput.value ?: ""
    }

    private fun checkPinCode(pin: String): Boolean = (pin == details?.otp)

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