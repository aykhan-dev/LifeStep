package az.rabita.lifestep.viewModel.fragment.home

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.content.AdsTransactionContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.CurrentStepModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DateModelPOJO
import az.rabita.lifestep.repository.AdsRepository
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@Suppress("UNCHECKED_CAST")
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)
    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val reportRepository = ReportRepository.getInstance(getDatabase(context))
    private val transactionsRepository = TransactionsRepository
    private val adsRepository = AdsRepository

    val searchInput: MutableLiveData<String> = MutableLiveData<String>().apply {
        observeForever { fetchSearchResults() }
    }

    private val _adsTransaction = MutableLiveData<AdsTransactionContentPOJO>()
    val adsTransaction: LiveData<AdsTransactionContentPOJO> get() = _adsTransaction

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var _listOfSearchResult = MutableLiveData<List<SearchResultContentPOJO>>()
    val listOfSearchResult: LiveData<List<SearchResultContentPOJO>> get() = _listOfSearchResult

    val weeklyStats = reportRepository.weeklyStats.asLiveData()

    val searchingState = MutableLiveData<UiState>()
    val uiState = MutableLiveData<UiState>()

    val cachedOwnProfileInfo = usersRepository.cachedProfileInfo

    fun fetchOwnProfileInfo() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, LANG_AZ)

            when (val response = usersRepository.getPersonalInfo(token, lang)) {
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    fun fetchSearchResults() {

        viewModelScope.launch {

            searchingState.value = UiState.Loading

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response =
                usersRepository.searchUserByFullName(token, lang, searchInput.value ?: "")) {
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

    fun fetchWeeklyStats() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val date = Calendar.getInstance().time
            val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

            when (val response =
                reportRepository.getWeeklyStats(token, lang, formatted.toString())) {
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    private fun sendCurrentStepData(steps: Long) {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val date = Calendar.getInstance().time
            val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

            val model = CurrentStepModelPOJO(
                count = steps,
                createdDate = formatted.toString()
            )

            when (val response = transactionsRepository.sendCurrentStepData(token, lang, model)) {
                is NetworkResult.Success<*> -> fetchWeeklyStats()
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    fun convertSteps() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val date = Calendar.getInstance().time
            val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

            val model = DateModelPOJO(date = formatted.toString())

            when (val response = transactionsRepository.convertSteps(token, lang, model)) {
                is NetworkResult.Success<*> -> fetchWeeklyStats()
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    fun createAdsTransaction() {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, LANG_AZ)

            val model = DateModelPOJO(date = getDateAndTime())

            when (val response = adsRepository.createAdsTransaction(token, lang, model)) {
                is NetworkResult.Success<*> -> {
                    val data = response.data as List<AdsTransactionContentPOJO>
                    if (data.isNotEmpty()) {
                        _adsTransaction.value = data[0]
                        _adsTransaction.value = null
                    }
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }

    }

    fun accessGoogleFit() {

        viewModelScope.launch(Dispatchers.IO) {
            val account = GoogleSignIn.getAccountForExtension(context, FITNESS_OPTIONS)
            val historyClient = Fitness.getHistoryClient(context, account)
            fetchStepCountFromGoogleFit(historyClient)
        }

    }

    private fun fetchStepCountFromGoogleFit(historyClient: HistoryClient) {
        historyClient
            .readDailyTotalFromLocalDevice(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                val total =
                    if (it.isEmpty) 0 else it.dataPoints[0].getValue(Field.FIELD_STEPS).asInt()
                        .toLong()
                sendCurrentStepData(total)
            }
            .addOnFailureListener { failure ->
                context.toast(failure.message)
            }
    }

    private suspend fun handleNetworkException(exception: String?) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
        else showMessageDialog(context.getString(R.string.no_internet_connection))
    }

    fun showMessageDialogSync(message: String?) {
        _errorMessage.value = message
        _errorMessage.value = null
    }

    suspend fun showMessageDialog(message: String?): Unit = withContext(Dispatchers.Main) {
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

    override fun onCleared() {
        super.onCleared()
        searchInput.removeObserver { }
    }

}