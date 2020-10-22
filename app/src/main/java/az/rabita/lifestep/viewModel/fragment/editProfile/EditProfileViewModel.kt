package az.rabita.lifestep.viewModel.fragment.editProfile

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.model.ChangedProfileDetailsModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
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

    val nameInput = MutableLiveData<String?>()
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

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (uiState.value == UiState.Loading) uiState.value = UiState.LoadingFinished
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun fetchPersonalInfo() {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            usersRepository.getPersonalInfoExceptionally(token, lang)

            uiState.value = UiState.LoadingFinished

        }

    }

    fun saveChanges() {

        if (!validateFields()) return

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = ChangedProfileDetailsModelPOJO(
                name = nameInput.value ?: "",
                surname = surnameInput.value ?: "",
                phoneNumber = phoneInput.value ?: ""
            )

            usersRepository.saveProfileDetailChanges(token, lang, model)

            uiState.value = UiState.LoadingFinished

        }

    }

    fun updateProfileImage(file: File) {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val requestFile: RequestBody =
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData("image", file.name, requestFile)

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            usersRepository.changeProfilePhoto(token, lang, body)

            fetchPersonalInfo()

            uiState.value = UiState.LoadingFinished

        }
    }

    private fun validateFields(): Boolean = when {
        (nameInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_name), MessageType.ERROR)
            false
        }
        (surnameInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_surname), MessageType.ERROR)
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

    private fun getString(res: Int) = context.getString(res)

}