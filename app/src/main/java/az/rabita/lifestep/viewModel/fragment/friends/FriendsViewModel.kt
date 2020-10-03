package az.rabita.lifestep.viewModel.fragment.friends

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pagingSource.FriendRequestsPagingSource
import az.rabita.lifestep.pagingSource.FriendsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.model.FriendshipActionModelPOJO
import az.rabita.lifestep.repository.FriendshipRepository
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences by lazy { PreferenceManager.getInstance(context) }

    private val reportRepository = ReportRepository.getInstance(getDatabase(context))
    private val friendshipRepository = FriendshipRepository

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val friendshipStats = reportRepository.friendshipStats.asLiveData()

    val uiState = MutableLiveData<UiState>()

    private val pagingConfig =
        PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false)

    val friendsListFlow = Pager(config = pagingConfig) {
        FriendsPagingSource(
            token = sharedPreferences.getStringElement(TOKEN_KEY, ""),
            lang = sharedPreferences.getIntegerElement(LANG_KEY, 10),
            service = ApiInitHelper.friendshipService,
            onErrorListener = { handleNetworkExceptionSync(it) },
            onExpireTokenListener = { startExpireTokenProcessSync() }
        )
    }.flow.cachedIn(viewModelScope)

    val friendsRequestListFlow = Pager(config = pagingConfig) {
        FriendRequestsPagingSource(
            token = sharedPreferences.getStringElement(TOKEN_KEY, ""),
            lang = sharedPreferences.getIntegerElement(LANG_KEY, 10),
            service = ApiInitHelper.friendshipService,
            onErrorListener = { handleNetworkExceptionSync(it) },
            onExpireTokenListener = { startExpireTokenProcessSync() }
        )
    }.flow.cachedIn(viewModelScope)

    fun processFriendshipRequest(userId: String, isAccepted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {

            uiState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)
            val model = FriendshipActionModelPOJO(userId)

            when (val response =
                friendshipRepository.processFriendRequest(token, lang, model, isAccepted)) {
                is NetworkState.Success<*> -> fetchFriendshipStats()
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)

        }
    }

    fun fetchFriendshipStats() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getFriendshipStats(token, lang)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }
        }
    }

    private fun handleNetworkExceptionSync(exception: String?) {
        if (context.isInternetConnectionAvailable()) showMessageDialogSync(exception)
        else showMessageDialogSync(context.getString(R.string.no_internet_connection))
    }

    private suspend fun handleNetworkException(exception: String?) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
        else showMessageDialog(context.getString(R.string.no_internet_connection))
    }

    private fun showMessageDialogSync(message: String?) {
        _errorMessage.value = message
        _errorMessage.value = null
    }

    private suspend fun showMessageDialog(message: String?): Unit = withContext(Dispatchers.Main) {
        _errorMessage.value = message
        _errorMessage.value = null
    }

    private fun startExpireTokenProcessSync() {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_eventExpiredToken.value == false) _eventExpiredToken.postValue(true)
    }

    private suspend fun startExpireTokenProcess(): Unit = withContext(Dispatchers.Main) {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_eventExpiredToken.value == false) _eventExpiredToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}