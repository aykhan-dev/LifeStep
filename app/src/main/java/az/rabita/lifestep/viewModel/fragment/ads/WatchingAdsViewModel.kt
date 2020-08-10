package az.rabita.lifestep.viewModel.fragment.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.AdsTransactionContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.ConvertStepsModelPOJO
import az.rabita.lifestep.repository.AdsRepository
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class WatchingAdsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val adsRepository = AdsRepository
    private val reportRepository = ReportRepository.getInstance(getDatabase(context))

    private lateinit var timer: ExtendedCountDownTimer

    private val _adsTransaction = MutableLiveData<AdsTransactionContentPOJO>()
    val adsTransaction: LiveData<AdsTransactionContentPOJO> get() = _adsTransaction

    private val _eventExpireToken = MutableLiveData<Boolean>()
    val eventExpireToken: LiveData<Boolean> get() = _eventExpireToken

    private val _eventCloseAdsPage = MutableLiveData<Boolean>()
    val eventCloseAdsPage: LiveData<Boolean> get() = _eventCloseAdsPage

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    val isClosingEnable = MutableLiveData<Boolean>().apply { value = false }
    val remainingTime = MutableLiveData<String>()
    val uiState = MutableLiveData<UiState>()

    fun sendAdsTransactionResult(
        transactionId: String,
        watchTime: Int,
        forBonusSteps: Boolean
    ) {
        viewModelScope.launch {

            uiState.postValue(UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, 10)

            val model = ConvertStepsModelPOJO(
                transactionId = transactionId,
                watchTime = watchTime,
                createdDate = getDateAndTime()
            )

            when (
                val response = if (forBonusSteps)
                    adsRepository.sendBonusAdsTransactionResult(token, lang, model)
                else
                    adsRepository.sendAdsTransactionResult(token, lang, model)
                ) {
                is NetworkState.Success<*> -> {
                    reportRepository.getWeeklyStats(token, lang, getDateAndTime())
                    uiState.postValue(UiState.LoadingFinished)
                    _eventCloseAdsPage.onOff()
                    return@launch
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.postValue(UiState.LoadingFinished)

        }
    }

    fun setupTimer(timeMillis: Long = 15000L, tickMillis: Long = 1000L) {
        timer = ExtendedCountDownTimer(
            timeMillis,
            tickMillis,
            { remainingTime.value = null; isClosingEnable.value = true },
            { remainingTime.value = "${it / 1000} ${context.getString(R.string.remaining)}" }
        )
    }

    fun startTimer() {
        timer.start()
    }

    fun pauseTimer() {
        timer.cancel()
    }

    private fun handleNetworkException(exception: String?) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
        else showMessageDialog(context.getString(R.string.no_internet_connection))
    }

    private fun showMessageDialog(message: String?) {
        _errorMessage.value = message
        _errorMessage.value = null
    }

    private fun startExpireTokenProcess() {
        sharedPreferences.setStringElement(TOKEN_KEY, "")
        if (_eventExpireToken.value == false) _eventExpireToken.value = true
    }

    fun endExpireTokenProcess() {
        _eventExpireToken.value = false
    }

    override fun onCleared() {
        super.onCleared()
        if (::timer.isInitialized) timer.cancel()
    }

}