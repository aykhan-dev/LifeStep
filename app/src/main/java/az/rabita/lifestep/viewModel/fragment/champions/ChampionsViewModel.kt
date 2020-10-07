@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.viewModel.fragment.champions

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
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

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message>()
    val errorMessage: LiveData<Message> get() = _errorMessage

    private val _listOfChampions = MutableLiveData<List<RankerContentPOJO>>()
    val listOfChampions: LiveData<List<RankerContentPOJO>> get() = _listOfChampions

    val cachedOwnProfileInfo = usersRepository.personalInfo.asLiveData()

    fun fetchDailyChampions() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getChampionsOfDay(token, lang)) {
                is NetworkResult.Success<*> -> run {
                    val data = response.data as List<RankerContentPOJO>
                    _listOfChampions.postValue(data)
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    fun fetchWeeklyChampions() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getChampionsOfWeek(token, lang)) {
                is NetworkResult.Success<*> -> run {
                    val data = response.data as List<RankerContentPOJO>
                    _listOfChampions.postValue(data)
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }
        }

    }

    fun fetchMonthlyChampions() {

        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getChampionsOfMonth(token, lang)) {
                is NetworkResult.Success<*> -> run {
                    val data = response.data as List<RankerContentPOJO>
                    _listOfChampions.postValue(data)
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }

    }

    private suspend fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(context.getString(R.string.no_internet_connection), MessageType.NO_INTERNET)
    }

    private suspend fun showMessageDialog(message: String, type: MessageType): Unit = withContext(Dispatchers.Main) {
        _errorMessage.value = Message(message, type)
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