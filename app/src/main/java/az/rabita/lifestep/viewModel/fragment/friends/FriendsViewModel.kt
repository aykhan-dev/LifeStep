package az.rabita.lifestep.viewModel.fragment.friends

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.ApiInitHelper
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pagingSource.FriendRequestsPagingSource
import az.rabita.lifestep.pagingSource.FriendsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.content.FriendContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendshipActionModelPOJO
import az.rabita.lifestep.repository.FriendshipRepository
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences by lazy { PreferenceManager.getInstance(context) }

    private val reportRepository = ReportRepository.getInstance(getDatabase(context))
    private val friendshipRepository = FriendshipRepository.getInstance(
        PaginationListeners(
            onErrorListener = { msg -> showMessageDialog(msg) },
            onExpireTokenListener = { startExpireTokenProcess() },
            onNetworkExceptionListener = { handleNetworkException(it) }
        )
    )

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
            onErrorListener = { showMessageDialog(it) },
            onExpireTokenListener = { startExpireTokenProcess() }
        )
    }.flow.cachedIn(viewModelScope)

    val friendsRequestListFlow = Pager(config = pagingConfig) {
        FriendRequestsPagingSource(
            token = sharedPreferences.getStringElement(TOKEN_KEY, ""),
            lang = sharedPreferences.getIntegerElement(LANG_KEY, 10),
            service = ApiInitHelper.friendshipService,
            onErrorListener = { showMessageDialog(it) },
            onExpireTokenListener = { startExpireTokenProcess() }
        )
    }.flow.cachedIn(viewModelScope)

    fun processFriendshipRequest(userId: String, isAccepted: Boolean) {
        viewModelScope.launch {

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
        viewModelScope.launch {
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