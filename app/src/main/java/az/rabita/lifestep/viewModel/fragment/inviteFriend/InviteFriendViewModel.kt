package az.rabita.lifestep.viewModel.fragment.inviteFriend

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult

import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.ContentsRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.INVITE_FRIENDS_GROUP_ID
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import az.rabita.lifestep.utils.onOff
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("UNCHECKED_CAST")
class InviteFriendViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val contentsRepository = ContentsRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventSendSharingMessage = MutableLiveData<Boolean>()
    val eventSendSharingMessage: LiveData<Boolean> get() = _eventSendSharingMessage

    val personalInfo = usersRepository.personalInfo.asLiveData()

    val inviteFriendContentsBody = contentsRepository.inviteFriendsContentBody.asLiveData()

    val inviteFriendContentMessage = contentsRepository.inviteFriendsContentMessage

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun fetchPersonalInfo() {

        viewModelScope.launch(exceptionHandler) {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            usersRepository.getPersonalInfoExceptionally(token, lang)

        }

    }

    fun fetchInviteFriendsContent() {

        viewModelScope.launch(exceptionHandler) {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            contentsRepository.getContent(token, lang, INVITE_FRIENDS_GROUP_ID)

        }

    }

    fun onSendInvitation() {
        if (!inviteFriendContentMessage.value?.content.isNullOrEmpty()) _eventSendSharingMessage.onOff()
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

}