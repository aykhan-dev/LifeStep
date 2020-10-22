@file:Suppress("UNCHECKED_CAST")

package az.rabita.lifestep.viewModel.fragment.profileDetails

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.dataHolder.AllInOneUserInfoHolder
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.custom.BarDiagram
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.extractDiagramData
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import az.rabita.lifestep.utils.notifyObservers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

class OwnProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    private val _dailyStats = MutableLiveData<BarDiagram.DiagramDataModel?>()
    val dailyStats: LiveData<BarDiagram.DiagramDataModel?> = _dailyStats

    private val _monthlyStats = MutableLiveData<BarDiagram.DiagramDataModel?>()
    val monthlyStats: LiveData<BarDiagram.DiagramDataModel?> = _monthlyStats

    val isDailyStatsShown = MutableLiveData(true)

    val cachedProfileInfo = usersRepository.personalInfo.asLiveData()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun fetchAllInOneProfileInfo() {

        viewModelScope.launch(exceptionHandler) {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val response = usersRepository.getOwnProfileInfo(token, lang)

            val data = (response as NetworkResult.Success<AllInOneUserInfoHolder>).data
            _dailyStats.postValue(extractDiagramData(data.dailyStats))
            _monthlyStats.postValue(extractDiagramData(data.monthlyStats))

        }

    }

    fun onDailyTextClick() {
        isDailyStatsShown.value = true
        _dailyStats.notifyObservers()
    }

    fun onMonthlyTextClick() {
        isDailyStatsShown.value = false
        _monthlyStats.notifyObservers()
    }

    private fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(
            exception,
            MessageType.ERROR
        )
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