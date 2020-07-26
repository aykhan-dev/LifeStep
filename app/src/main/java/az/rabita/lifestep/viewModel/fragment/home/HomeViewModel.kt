package az.rabita.lifestep.viewModel.fragment.home

import android.app.Activity
import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.*
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
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
import kotlinx.coroutines.launch
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

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var _listOfSearchResult = MutableLiveData<List<SearchResultContentPOJO>>()
    val listOfSearchResult: LiveData<List<SearchResultContentPOJO>> get() = _listOfSearchResult

    val weeklyStats = reportRepository.weeklyStats.asLiveData()

    val searchingState = MutableLiveData<UiState>()
    val uiState = MutableLiveData<UiState>()

    fun fetchSearchResults() {
        viewModelScope.launch {

            searchingState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response =
                usersRepository.searchUserByFullName(token, lang, searchInput.value ?: "")) {
                is NetworkState.Success<*> -> {
                    val data = response.data as List<SearchResultContentPOJO>
                    _listOfSearchResult.value = data
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            searchingState.postValue(UiState.LoadingFinished)

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
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
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
                is NetworkState.Success<*> -> fetchWeeklyStats()
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
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
                is NetworkState.Success<*> -> fetchWeeklyStats()
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    fun createAdsTransaction() {
        viewModelScope.launch {

            uiState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, 10)

            val model = DateModelPOJO(date = getDateAndTime())

            when (val response = adsRepository.createAdsTransaction(token, lang, model)) {
                is NetworkState.Success<*> -> {
                    val data = response.data as List<AdsTransactionContentPOJO>
                    if (data.isNotEmpty()) {
                        _adsTransaction.value = data[0]
                        _adsTransaction.value = null
                    }
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)

        }
    }

    fun accessGoogleFit(activity: Activity) {
        viewModelScope.launch {
            val account = GoogleSignIn.getAccountForExtension(activity, FITNESS_OPTIONS)
            val historyClient = Fitness.getHistoryClient(activity, account)
            fetchStepCountFromGoogleFit(historyClient)
        }
    }

    private fun fetchStepCountFromGoogleFit(historyClient: HistoryClient) {
        viewModelScope.launch {
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

    override fun onCleared() {
        super.onCleared()
        searchInput.removeObserver { }
    }

}