package az.rabita.lifestep.viewModel.fragment.detailedInfo

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.apiPOJO.content.AssocationDetailsContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DonateStepModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DonorsModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.AssocationsRepository
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.getDateAndTime
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import az.rabita.lifestep.utils.onOff
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("UNCHECKED_CAST")
class DetailedInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val transactionsRepository = TransactionsRepository
    private val assocationsRepository = AssocationsRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventShowCongratsDialog = MutableLiveData<Boolean>()
    val eventShowCongratsDialog: LiveData<Boolean> get() = _eventShowCongratsDialog

    private val _assocationDetails = MutableLiveData<AssocationDetailsContentPOJO>()
    val assocationDetails: LiveData<AssocationDetailsContentPOJO> get() = _assocationDetails

    private val _topDonorsList = MutableLiveData<List<RankerContentPOJO>>()
    val topDonorsList: LiveData<List<RankerContentPOJO>> get() = _topDonorsList

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    val profileInfo = usersRepository.personalInfo.asLiveData()

    val uiState = MutableLiveData<UiState?>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (uiState.value == UiState.Loading) uiState.value = UiState.LoadingFinished
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun start(assocationId: String) {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val requests = listOf(
                async { fetchPersonalInfo() },
                async { fetchDetailedInfo(assocationId) }
            )

            requests.awaitAll()

            uiState.value = UiState.LoadingFinished

        }

    }

    suspend fun fetchDetailedInfo(assocationId: String) {

        val token = sharedPreferences.token
        val lang = sharedPreferences.langCode

        val response = assocationsRepository.getAssocationDetailsExceptionally(
            token,
            lang,
            assocationId
        )

        val data = (response as NetworkResult.Success<List<AssocationDetailsContentPOJO>>).data
        _assocationDetails.value = data[0]
        fetchTopDonorsList(data[0].id)

    }

    private suspend fun fetchTopDonorsList(assocationId: String) {

        val token = sharedPreferences.token
        val lang = sharedPreferences.langCode

        val model = DonorsModelPOJO(userId = assocationId, pageNumber = 1)

        val response = usersRepository.getDonorsOnlyForPost(token, lang, model)

        val data = (response as NetworkResult.Success<List<RankerContentPOJO>>).data
        _topDonorsList.postValue(data)

    }

    private suspend fun fetchPersonalInfo() {

        val token = sharedPreferences.token
        val lang = sharedPreferences.langCode

        usersRepository.getPersonalInfoExceptionally(token, lang)

    }

    fun donateStep(postId: String, assocationId: String, amount: Long, isPrivate: Boolean) {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val formatted = getDateAndTime()

            val model = DonateStepModelPOJO(
                id = postId,
                isPrivate = isPrivate,
                count = amount,
                createdDate = formatted,
            )

            transactionsRepository.donateStep(token, lang, model)

            fetchDetailedInfo(assocationId)
            _eventShowCongratsDialog.onOff()

            uiState.value = UiState.LoadingFinished

        }

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