package az.rabita.lifestep.viewModel.fragment.searchResults

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
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.*

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    val cachedOwnProfileInfo = usersRepository.cachedProfileInfo

    fun fetchSearchResults(searchInput: String) {
        viewModelScope.launch(Dispatchers.IO) {

            searchingState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response =
                usersRepository.searchUserByFullName(token, lang, searchInput)) {
                is NetworkState.Success<*> -> {
                    val data = response.data as List<SearchResultContentPOJO>
                    _listOfSearchResult.postValue(data)
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            searchingState.postValue(UiState.LoadingFinished)

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