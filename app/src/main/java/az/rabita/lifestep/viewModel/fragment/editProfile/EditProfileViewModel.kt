package az.rabita.lifestep.viewModel.fragment.editProfile

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.model.ChangedProfileDetailsModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventCloseEditProfilePage = MutableLiveData<Boolean>()
    val eventCloseEditProfilePage: LiveData<Boolean> get() = _eventCloseEditProfilePage

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    val nameInput = MutableLiveData<String>()
    val surnameInput = MutableLiveData<String>()
    val emailInput = MutableLiveData<String>()
    val phoneInput = MutableLiveData<String>()

    val profileInfo by lazy {
        val data = usersRepository.personalInfo.asLiveData()
        nameInput.value = data.value?.name ?: ""
        surnameInput.value = data.value?.surname ?: ""
        phoneInput.value = data.value?.phone ?: ""
        data
    }

    val uiState = MutableLiveData<UiState>()

    fun fetchPersonalInfo() {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            when (val response = usersRepository.getPersonalInfo(token, lang)) {
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }

    }

    fun saveChanges() {

        if (!validateFields()) return

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = ChangedProfileDetailsModelPOJO(
                name = nameInput.value ?: "",
                surname = surnameInput.value ?: "",
                phoneNumber = phoneInput.value ?: ""
            )

            when (val response = usersRepository.saveProfileDetailChanges(token, lang, model)) {
                is NetworkResult.Success<*> -> _eventCloseEditProfilePage.onOff()
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }

    }

    fun updateProfileImage(file: File) {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val requestFile: RequestBody =
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData("image", file.name, requestFile)

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            when (val response = usersRepository.changeProfilePhoto(token, lang, body)) {
                is NetworkResult.Success<*> -> fetchPersonalInfo()
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }
    }

    private fun validateFields(): Boolean = when {
        (nameInput.value ?: "").isEmpty() -> {
            showMessageDialogSync(getString(R.string.invalid_name), MessageType.ERROR)
            false
        }
        (surnameInput.value ?: "").isEmpty() -> {
            showMessageDialogSync(getString(R.string.invalid_surname), MessageType.ERROR)
            false
        }
        (phoneInput.value ?: "").isEmpty() -> {
            showMessageDialogSync(getString(R.string.invalid_phone), MessageType.ERROR)
            false
        }
        else -> true
    }

    private fun handleNetworkExceptionSync(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialogSync(
            exception,
            MessageType.ERROR
        )
        else showMessageDialogSync(
            context.getString(R.string.no_internet_connection),
            MessageType.NO_INTERNET
        )
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

    private fun startExpireTokenProcessSync() {
        sharedPreferences.token = ""
        if (_eventExpiredToken.value == false) _eventExpiredToken.postValue(true)
    }

    private suspend fun startExpireTokenProcess(): Unit = withContext(Dispatchers.Main) {
        sharedPreferences.token = ""
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

    private fun getString(res: Int) = context.getString(res)

}