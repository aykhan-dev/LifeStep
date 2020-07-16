package az.rabita.lifestep.viewModel.fragment.aboutUs

import android.app.Application
import az.rabita.lifestep.utils.DEFAULT_LANG
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.repository.ContentsRepository
import az.rabita.lifestep.utils.ABOUT_US_GROUP_ID
import az.rabita.lifestep.utils.DEFAULT_LANG
import az.rabita.lifestep.utils.LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import kotlinx.coroutines.launch
import timber.log.Timber

class AboutUsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val contentsRepository = ContentsRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val body = contentsRepository.aboutUsContentBody

    fun fetchAboutUsPageContent() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
             val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = contentsRepository.getContent(token, lang, ABOUT_US_GROUP_ID)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showDialog(response.error)
                is NetworkState.UnhandledHttpError -> showDialog(response.error)
                is NetworkState.NetworkException -> handleError(response.exception)
            }

        }
    }

    private fun handleError(exception: String?) = Timber.e(exception)

    private fun showDialog(message: String?) {
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

}