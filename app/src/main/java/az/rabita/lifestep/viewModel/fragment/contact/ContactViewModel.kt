package az.rabita.lifestep.viewModel.fragment.contact

import android.app.Application
import kotlinx.coroutines.Dispatchers

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.repository.ContentsRepository
import az.rabita.lifestep.utils.*

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val contentsRepository = ContentsRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val listOfContacts = contentsRepository.contactDetails

    fun fetchContactPageContent() {
        viewModelScope.launch(Dispatchers.IO) {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = contentsRepository.getContent(token, lang, CONTACTS_GROUP_ID)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    private suspend fun handleNetworkException(exception: String?) {
            if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
            else showMessageDialog(context.getString(R.string.no_internet_connection))
        }

    private suspend fun showMessageDialog(message: String?): Unit = withContext(Dispatchers.Main) {
        _errorMessage.value = message
        _errorMessage.value = null
    }

    private suspend fun startExpireTokenProcess(): Unit = withContext(Dispatchers.Main) {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}