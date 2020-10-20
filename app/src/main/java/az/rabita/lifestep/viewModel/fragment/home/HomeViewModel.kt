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
import az.rabita.lifestep.pojo.apiPOJO.model.ConvertStepsModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.CurrentStepModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DateModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.AdsRepository
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.ads.AdsDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.FITNESS_OPTIONS
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.getDateAndTime
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.tasks.Tasks
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

    val searchInput: MutableLiveData<String> = MutableLiveData<String>()

    private val _adsTransaction = MutableLiveData<AdsTransactionContentPOJO>()
    val adsTransaction: LiveData<AdsTransactionContentPOJO> get() = _adsTransaction

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    private var _listOfSearchResult = MutableLiveData<List<SearchResultContentPOJO>>()
    val listOfSearchResult: LiveData<List<SearchResultContentPOJO>> get() = _listOfSearchResult

    val weeklyStats = reportRepository.weeklyStats.asLiveData()

    val searchingState = MutableLiveData<UiState?>()
    val uiState = MutableLiveData<UiState>()

    val cachedOwnProfileInfo = usersRepository.cachedProfileInfo

    var adsGenerated = false

    private var steps = 0L
    private var calories = 0f
    private var distance = 0f

    fun fetchOwnProfileInfo() {

        viewModelScope.launch {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

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

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

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

            uiState.value = UiState.LoadingFinished

        }

    }

    fun fetchWeeklyStats() {

        viewModelScope.launch {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

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

    private fun sendCurrentStepData() {

        viewModelScope.launch {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val date = Calendar.getInstance().time
            val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

            val model = CurrentStepModelPOJO(
                count = steps,
                calorie = calories,
                kilometer = distance,
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

    fun convertSteps(data: Map<String, Any>) {

        val transactionId = data[AdsDialog.ID_KEY] as String
        val isForBonusSteps = data[AdsDialog.IS_FOR_BONUS_STEPS_KEY] as Boolean
        val watchTime = data[AdsDialog.TOTAL_WATCH_TIME_KEY] as Int

        if (isForBonusSteps) return

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = ConvertStepsModelPOJO(
                transactionId = transactionId,
                watchTime = watchTime,
                createdDate = getDateAndTime()
            )

            when (val response = adsRepository.sendAdsTransactionResult(token, lang, model)) {
                is NetworkResult.Success<*> -> fetchWeeklyStats()
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            uiState.value = UiState.LoadingFinished

        }

    }

    fun createAdsTransaction() {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = DateModelPOJO(date = getDateAndTime())

            when (val response = adsRepository.createAdsTransaction(token, lang, model)) {
                is NetworkResult.Success<*> -> {
                    val data = response.data as List<AdsTransactionContentPOJO>
                    if (data.isNotEmpty()) {
                        adsGenerated = true
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

            Tasks.whenAll(
                fetchStepCount(historyClient),
                fetchCalories(historyClient),
                fetchDistance(historyClient)
            ).addOnSuccessListener {
                sendCurrentStepData()
            }

        }

    }

    private fun fetchStepCount(historyClient: HistoryClient) = historyClient
        .readDailyTotalFromLocalDevice(DataType.TYPE_STEP_COUNT_DELTA)
        .addOnSuccessListener { data ->
            val steps =
                if (data.isEmpty) 0
                else data.dataPoints[0].getValue(Field.FIELD_STEPS).asInt().toLong()
            this.steps = steps
        }

    private fun fetchCalories(historyClient: HistoryClient) = historyClient
        .readDailyTotalFromLocalDevice(DataType.TYPE_CALORIES_EXPENDED)
        .addOnSuccessListener { data ->
            val calories =
                if (data.isEmpty) 0f
                else data.dataPoints[0].getValue(Field.FIELD_CALORIES).asFloat()
            this.calories = calories
        }

    private fun fetchDistance(historyClient: HistoryClient) = historyClient
        .readDailyTotalFromLocalDevice(DataType.TYPE_DISTANCE_DELTA)
        .addOnSuccessListener { data ->
            val distance =
                if (data.isEmpty) 0f
                else data.dataPoints[0].getValue(Field.FIELD_DISTANCE).asFloat()
            this.distance = distance / 1000f
        }

    private suspend fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(
            context.getString(R.string.no_internet_connection),
            MessageType.NO_INTERNET
        )
    }

    fun showMessageDialogSync(message: String, type: MessageType) {
        _errorMessage.value = Message(message, type)
        _errorMessage.value = null
    }

    suspend fun showMessageDialog(message: String, type: MessageType): Unit =
        withContext(Dispatchers.Main) {
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

    override fun onCleared() {
        super.onCleared()
        searchInput.removeObserver { }
    }

}