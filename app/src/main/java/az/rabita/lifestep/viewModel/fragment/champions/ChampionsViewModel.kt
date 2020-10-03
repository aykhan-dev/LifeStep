@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.viewModel.fragment.champions

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.DEFAULT_LANG
import az.rabita.lifestep.utils.LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChampionsViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val reportRepository = ReportRepository.getInstance(getDatabase(context))

    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _listOfChampions = MutableLiveData<List<RankerContentPOJO>>()
    val listOfChampions: LiveData<List<RankerContentPOJO>> get() = _listOfChampions

    val cachedOwnProfileInfo = usersRepository.personalInfo.asLiveData()

    fun fetchDailyChampions() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getChampionsOfDay(token, lang)) {
                is NetworkState.Success<*> -> run {
                    val data = response.data as List<RankerContentPOJO>
                    _listOfChampions.postValue(data)
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }
        }
    }

    fun fetchWeeklyChampions() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getChampionsOfWeek(token, lang)) {
                is NetworkState.Success<*> -> run {
                    val data = response.data as List<RankerContentPOJO>
                    _listOfChampions.postValue(data)
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }
        }
    }

    fun fetchMonthlyChampions() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getChampionsOfMonth(token, lang)) {
                is NetworkState.Success<*> -> run {
                    val data = response.data as List<RankerContentPOJO>
                    _listOfChampions.postValue(data)
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }
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