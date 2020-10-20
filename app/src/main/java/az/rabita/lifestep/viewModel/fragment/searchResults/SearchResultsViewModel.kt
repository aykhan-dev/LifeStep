package az.rabita.lifestep.viewModel.fragment.searchResults

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class SearchResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)
    private val usersRepository = UsersRepository.getInstance(getDatabase(context))

    private val _listOfSearchResult = MutableLiveData<List<SearchResultContentPOJO>>()
    val listOfSearchResult: LiveData<List<SearchResultContentPOJO>> = _listOfSearchResult

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    val searchingState = MutableLiveData<UiState>()

    val cachedOwnProfileInfo = usersRepository.cachedProfileInfo

    fun fetchSearchResults(searchInput: String) {

        viewModelScope.launch {

            searchingState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            when (val response =
                usersRepository.searchUserByFullName(token, lang, searchInput)) {
                is NetworkResult.Success<*> -> {
                    val data = response.data as List<SearchResultContentPOJO>
                    _listOfSearchResult.postValue(data)
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            searchingState.value = UiState.LoadingFinished

        }

    }

    private suspend fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(context.getString(R.string.no_internet_connection), MessageType.NO_INTERNET)
    }

    private suspend fun showMessageDialog(message: String, type: MessageType): Unit = withContext(Dispatchers.Main) {
        _errorMessage.value = Message(message, type)
        _errorMessage.value = null
    }

    private suspend fun startExpireTokenProcess(): Unit = withContext(Dispatchers.Main) {
        sharedPreferences.token = ""
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}