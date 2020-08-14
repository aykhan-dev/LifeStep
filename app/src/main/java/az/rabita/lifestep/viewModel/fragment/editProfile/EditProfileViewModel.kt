package az.rabita.lifestep.viewModel.fragment.editProfile

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.model.ChangedProfileDetailsModelPOJO
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventCloseEditProfilePage = MutableLiveData<Boolean>()
    val eventCloseEditProfilePage: LiveData<Boolean> get() = _eventCloseEditProfilePage

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

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

            uiState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.getPersonalInfo(token, lang)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)

        }
    }

    fun saveChanges() {
        viewModelScope.launch {

            if (!validateFields()) return@launch

            uiState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val model = ChangedProfileDetailsModelPOJO(
                name = nameInput.value ?: "",
                surname = surnameInput.value ?: "",
                phoneNumber = phoneInput.value ?: ""
            )

            when (val response = usersRepository.saveProfileDetailChanges(token, lang, model)) {
                is NetworkState.Success<*> -> _eventCloseEditProfilePage.onOff()
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)

        }
    }

    fun updateProfileImage(file: File) {
        viewModelScope.launch {
            uiState.postValue(UiState.Loading)

            val requestFile: RequestBody =
                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)

            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData("image", file.name, requestFile)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.changeProfilePhoto(token, lang, body)) {
                is NetworkState.Success<*> -> fetchPersonalInfo()
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)
        }
    }

    private fun validateFields(): Boolean = when {
        (nameInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_name))
            false
        }
        (surnameInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_surname))
            false
        }
        (phoneInput.value ?: "").isEmpty() -> {
            showMessageDialog(getString(R.string.invalid_phone))
            false
        }
        else -> true
    }

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


}