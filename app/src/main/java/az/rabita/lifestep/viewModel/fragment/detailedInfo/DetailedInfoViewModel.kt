package az.rabita.lifestep.viewModel.fragment.detailedInfo

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.content.AssocationDetailsContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DonateStepModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DonorsModelPOJO
import az.rabita.lifestep.repository.AssocationsRepository
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class DetailedInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val transactionsRepository = TransactionsRepository
    private val assocationsRepository = AssocationsRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventShowDonateStepsDialog = MutableLiveData<Boolean>()
    val eventShowDonateStepsDialog: LiveData<Boolean> get() = _eventShowDonateStepsDialog

    private val _eventShowCongratsDialog = MutableLiveData<Boolean>()
    val eventShowCongratsDialog: LiveData<Boolean> get() = _eventShowCongratsDialog

    private val _assocationDetails = MutableLiveData<AssocationDetailsContentPOJO>()
    val assocationDetails: LiveData<AssocationDetailsContentPOJO> get() = _assocationDetails

    private val _topDonorsList = MutableLiveData<List<RankerContentPOJO>>()
    val topDonorsList: LiveData<List<RankerContentPOJO>> get() = _topDonorsList

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val donatedStepInput = MutableLiveData<String>().apply {
        observeForever {
            val input = if (value?.isEmpty() != false) 0 else (value ?: "0").toLong()
            if (input > profileInfo.value?.balance ?: 0) postValue("${profileInfo.value?.balance ?: 0}")
        }
    }

    val profileInfo = usersRepository.personalInfo.asLiveData()

    val uiState = MutableLiveData<UiState>()

    fun fetchDetailedInfo(assocationId: String) {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response =
                assocationsRepository.getAssocationDetails(token, lang, assocationId)) {
                is NetworkResult.Success<*> -> {
                    val data = response.data as List<AssocationDetailsContentPOJO>
                    _assocationDetails.value = data[0]
                    fetchTopDonorsList(data[0].id)
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }

    }

    private fun fetchTopDonorsList(assocationId: String) {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val model = DonorsModelPOJO(userId = assocationId, pageNumber = 1)

            when (val response = usersRepository.getDonorsOnlyForPost(token, lang, model)) {
                is NetworkResult.Success<*> -> {
                    val data = response.data as List<RankerContentPOJO>
                    _topDonorsList.postValue(data)
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    fun fetchPersonalInfo() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.getPersonalInfo(token, lang)) {
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }
    }

    fun donateStep(postId: String?, isPrivate: Boolean) {

        viewModelScope.launch {

            postId?.let {

                uiState.value = UiState.Loading

                val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
                val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

                val formatted = getDateAndTime()

                val model = DonateStepModelPOJO(
                    id = it,
                    isPrivate = isPrivate,
                    count = (donatedStepInput.value ?: "0").toLong(),
                    createdDate = formatted,
                )

                when (val response = transactionsRepository.donateStep(token, lang, model)) {
                    is NetworkResult.Success<*> -> _eventShowCongratsDialog.onOff()
                    is NetworkResult.Failure -> when (response.type) {
                        NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                        else -> handleNetworkException(response.message)
                    }
                }

                uiState.value = UiState.LoadingFinished

            } ?: showMessageDialog("Null argument")

        }

    }

    fun donateStep(postId: String, amount: Long, isPrivate: Boolean) {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val formatted = getDateAndTime()

            val model = DonateStepModelPOJO(
                id = postId,
                isPrivate = isPrivate,
                count = amount,
                createdDate = formatted,
            )

            when (val response = transactionsRepository.donateStep(token, lang, model)) {
                is NetworkResult.Success<*> -> {
                    fetchDetailedInfo(postId)
                    _eventShowCongratsDialog.onOff()
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

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