package az.rabita.lifestep.viewModel.fragment.inviteFriend

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.ContentContentPOJO
import az.rabita.lifestep.repository.ContentsRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class InviteFriendViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val contentsRepository = ContentsRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventSendSharingMessage = MutableLiveData<Boolean>()
    val eventSendSharingMessage: LiveData<Boolean> get() = _eventSendSharingMessage

    val personalInfo = usersRepository.personalInfo.asLiveData()

    val inviteFriendContentsBody = contentsRepository.inviteFriendsContentBody

    private val _inviteFriendContentsMessage = MutableLiveData<String>()
    val inviteFriendContentsMessage get() = _inviteFriendContentsMessage

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchPersonalInfo() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.getPersonalInfo(token, lang)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> showMessageDialog(response.exception)
            }

        }
    }

    fun fetchInviteFriendsContent() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response =
                contentsRepository.getContent(token, lang, INVITE_FRIENDS_GROUP_ID)) {
                is NetworkState.Success<*> -> {
                    val data = response.data as List<ContentContentPOJO>
                    _inviteFriendContentsMessage.value = data[1].text
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> showMessageDialog(response.exception)
            }

        }
    }

    fun onSendInvitation() = viewModelScope.launch {
        if (!inviteFriendContentsMessage.value.isNullOrEmpty()) _eventSendSharingMessage.onOff()
        else showMessageDialog(NO_INTERNET_CONNECTION)
    }

    fun showMessageDialog(message: String?) {
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