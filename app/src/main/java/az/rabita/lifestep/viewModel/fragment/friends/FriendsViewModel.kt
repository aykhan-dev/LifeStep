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
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pagingSource.FriendRequestsPagingSource
import az.rabita.lifestep.pagingSource.FriendsPagingSource
import az.rabita.lifestep.pojo.apiPOJO.model.FriendshipActionModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.FriendshipRepository
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.NETWORK_PAGE_SIZE
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences by lazy { PreferenceManager.getInstance(context) }

    private val reportRepository = ReportRepository.getInstance(getDatabase(context))
    private val friendshipRepository = FriendshipRepository

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    val friendshipStats = reportRepository.friendshipStats.asLiveData()

    val uiState = MutableLiveData<UiState?>()

    private val pagingConfig =
        PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (uiState.value == UiState.Loading) uiState.value = UiState.LoadingFinished
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    private val friendsPagingSource = FriendsPagingSource(
        token = sharedPreferences.token,
        lang = sharedPreferences.langCode,
        service = ApiInitHelper.friendshipService,
        onExpiredToken = { withContext(Dispatchers.Main) { startExpireTokenProcess() } },
        onError = { withContext(Dispatchers.Main) { handleNetworkExceptionSync(it) } }
    )

    private val friendshipRequestsPagingSource = FriendRequestsPagingSource(
        token = sharedPreferences.token,
        lang = sharedPreferences.langCode,
        service = ApiInitHelper.friendshipService,
        onExpiredToken = { withContext(Dispatchers.Main) { startExpireTokenProcess() } },
        onError = { withContext(Dispatchers.Main) { handleNetworkExceptionSync(it) } }
    )

    val friendsListFlow
        get() = Pager(config = pagingConfig) { friendsPagingSource }.flow.cachedIn(viewModelScope)

    val friendsRequestListFlow
        get() = Pager(config = pagingConfig) { friendshipRequestsPagingSource }.flow.cachedIn(
            viewModelScope
        )

    fun processFriendshipRequest(userId: String, isAccepted: Boolean) {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = FriendshipActionModelPOJO(userId)

            friendshipRepository.processFriendRequest(token, lang, model, isAccepted)
            fetchFriendshipStats()

            uiState.value = UiState.LoadingFinished

        }

    }

    fun fetchFriendshipStats() {

        viewModelScope.launch(exceptionHandler) {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            reportRepository.getFriendshipStats(token, lang)

        }

    }

    private fun handleNetworkExceptionSync(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(
            context.getString(R.string.no_internet_connection),
            MessageType.NO_INTERNET
        )
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

    fun refresh() {
        friendsPagingSource.invalidate()
        friendshipRequestsPagingSource.invalidate()
    }

}