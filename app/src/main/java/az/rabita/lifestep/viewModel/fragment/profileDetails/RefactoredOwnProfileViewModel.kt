package az.rabita.lifestep.viewModel.fragment.profileDetails

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.DailyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.MonthlyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendRequestModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.SendStepModelPOJO
import az.rabita.lifestep.pojo.dataHolder.AllInOneUserInfoHolder
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.custom.BarDiagram
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch
import java.util.*

class RefactoredOwnProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val _eventExpiredToken = MutableLiveData<Boolean>(false)
    val eventExpiredToken: LiveData<Boolean> = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _dailyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val dailyStats: LiveData<BarDiagram.DiagramDataModel> = _dailyStats

    private val _monthlyStats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val monthlyStats: LiveData<BarDiagram.DiagramDataModel> = _monthlyStats

    val isDailyStatsShown = MutableLiveData<Boolean>(true)

    val cachedProfileInfo = usersRepository.personalInfo.asLiveData()

    fun fetchAllInOneProfileInfo() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = usersRepository.getUserInfoAllInOne(token, lang)) {
                is NetworkState.Success<*> -> run {
                    val data = response.data as AllInOneUserInfoHolder
                    _dailyStats.postValue(extractDiagramData(data.dailyStats))
                    _monthlyStats.postValue(extractDiagramData(data.monthlyStats))
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

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