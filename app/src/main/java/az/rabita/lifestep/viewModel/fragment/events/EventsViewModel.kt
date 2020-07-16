package az.rabita.lifestep.viewModel.fragment.events

import android.app.Application
import az.rabita.lifestep.utils.DEFAULT_LANG
import androidx.lifecycle.*
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.repository.CategoriesRepository
import az.rabita.lifestep.utils.LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import kotlinx.coroutines.launch

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val categoriesRepository = CategoriesRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val listOfCategories = categoriesRepository.listOfCategories.asLiveData()

    fun fetchAllCategories() = viewModelScope.launch {

        val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
        val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

        when (val response = categoriesRepository.getCategories(token, lang)) {
            is NetworkState.ExpiredToken -> startExpireTokenProcess()
            is NetworkState.HandledHttpError -> showMessageDialog(response.error)
            is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
            is NetworkState.NetworkException -> showMessageDialog(response.exception)
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

}