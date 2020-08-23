package az.rabita.lifestep.viewModel.fragment.profileDetails

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.DailyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.MonthlyContentPOJO
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.custom.BarDiagram
import az.rabita.lifestep.utils.DEFAULT_LANG
import az.rabita.lifestep.utils.LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class ProfileDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val reportRepository = ReportRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _dailyTextSelected = MutableLiveData<Boolean>().apply { value = true }
    val dailyTextSelected: LiveData<Boolean> get() = _dailyTextSelected

    private val _monthlyTextSelected = MutableLiveData<Boolean>().apply { value = false }
    val monthlyTextSelected: LiveData<Boolean> get() = _monthlyTextSelected

    private val _stats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val stats: LiveData<BarDiagram.DiagramDataModel> get() = _stats

    private val listOfDailyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    private var listOfMonthlyStats = MutableLiveData<BarDiagram.DiagramDataModel>()

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val profileInfo = usersRepository.personalInfo.asLiveData()

    fun fetchProfile() {
        viewModelScope.launch {
            fetchProfileDetails()
            refreshDailyStats()
            refreshMonthlyStats()
        }
    }

    private suspend fun fetchProfileDetails() {
        val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
        val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

        when (val response = usersRepository.getPersonalInfo(token, lang)) {
            is NetworkState.Success<*> -> {
                refreshDailyStats()
                refreshMonthlyStats()
            }
            is NetworkState.ExpiredToken -> startExpireTokenProcess()
            is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
            is NetworkState.HandledHttpError -> showMessageDialog(response.error)
            is NetworkState.NetworkException -> handleNetworkException(response.exception)
        }
    }

    fun onDailyTextClick() {
        _dailyTextSelected.value = true
        _monthlyTextSelected.value = false
        _stats.value = listOfDailyStats.value
    }

    fun onMonthlyTextClick() {
        _monthlyTextSelected.value = true
        _dailyTextSelected.value = false
        _stats.value = listOfMonthlyStats.value
    }

    private suspend fun refreshDailyStats() {
        profileInfo.value?.let { info ->

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response =
                reportRepository.getDailyStats(token, lang, info.id)) {
                is NetworkState.Success<*> -> run {
                    val data = response.data as List<DailyContentPOJO>
                    listOfDailyStats.postValue(extractDiagramData(data))
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    private suspend fun refreshMonthlyStats() {
        profileInfo.value?.let { info ->

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response =
                reportRepository.getMonthlyStats(token, lang, info.id)) {
                is NetworkState.Success<*> -> run {
                    val data = response.data as List<MonthlyContentPOJO>
                    listOfMonthlyStats.postValue(extractDiagramData(data))
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    private fun extractDiagramData(data: List<*>): BarDiagram.DiagramDataModel {
        var maxValue = 0L
        val columns = mutableListOf<String>()
        val values = mutableListOf<Long>()

        for (i in data) {
            when (i) {
                is MonthlyContentPOJO -> {
                    maxValue = maxValue.coerceAtLeast(i.count)
                    columns.add(i.monthName)
                    values.add(i.count)
                }
                is DailyContentPOJO -> {
                    maxValue = maxValue.coerceAtLeast(i.count)
                    columns.add(i.createdDate.substring(8, 10))
                    values.add(i.count)
                }
            }
        }

        return BarDiagram.DiagramDataModel(
            maxValue = maxValue,
            columnTexts = columns,
            values = values
        )
    }

    private fun handleNetworkException(exception: String?) {
        viewModelScope.launch {
            if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
            else showMessageDialog(context.getString(R.string.no_internet_connection))
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