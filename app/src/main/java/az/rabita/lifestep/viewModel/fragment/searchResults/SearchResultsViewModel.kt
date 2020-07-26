package az.rabita.lifestep.viewModel.fragment.searchResults

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class SearchResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)
    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private val _listOfSearchResult = MutableLiveData<List<SearchResultContentPOJO>>()
    val listOfSearchResult: LiveData<List<SearchResultContentPOJO>> = _listOfSearchResult

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val searchingState = MutableLiveData<UiState>()

    fun fetchSearchResults(searchInput: String) {
        viewModelScope.launch {

            searchingState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response =
                usersRepository.searchUserByFullName(token, lang, searchInput)) {
                is NetworkState.Success<*> -> {
                    val data = response.data as List<SearchResultContentPOJO>
                    _listOfSearchResult.value = data
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            searchingState.postValue(UiState.LoadingFinished)

        }
    }

    private fun handleNetworkException(exception: String?) {
        viewModelScope.launch {
            if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
            else showMessageDialog(NO_INTERNET_CONNECTION)
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