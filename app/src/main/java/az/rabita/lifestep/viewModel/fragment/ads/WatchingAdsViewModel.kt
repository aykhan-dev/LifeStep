package az.rabita.lifestep.viewModel.fragment.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.content.AdsTransactionContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.ConvertStepsModelPOJO
import az.rabita.lifestep.repository.AdsRepository
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    val isMuted = MutableLiveData(false)
    val remainingTime = MutableLiveData<Long>()
    val uiState = MutableLiveData<UiState>()

    var isSuccessfullyWatched = false

    fun sendAdsTransactionResult(
        transactionId: String,
        watchTime: Int,
        forBonusSteps: Boolean,
        totalWatchTime: Int
    ) {

        viewModelScope.launch {

            uiState.value = UiState.Loading

            isSuccessfullyWatched = watchTime == totalWatchTime

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, LANG_AZ)

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
                is NetworkResult.Success<*> -> {
                    reportRepository.getWeeklyStats(token, lang, getDateAndTime())
                    _eventCloseAdsPage.onOff()
                }
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

            isSuccessfullyWatched = false

            uiState.value = UiState.LoadingFinished

        }

    }

    fun setupTimer(timeMillis: Long = 15000L, tickMillis: Long = 1000L) {
        timer = ExtendedCountDownTimer(
            timeMillis,
            tickMillis,
            { remainingTime.value = null; },
            { remainingTime.value = it / 1000 }
        )
    }

    fun startTimer() {
        timer.start()
    }

    fun pauseTimer() {
        timer.cancel()
    }

    fun muteOrUnmute() {
        isMuted.value = !(isMuted.value ?: false)
    }

    private suspend fun handleNetworkException(exception: String?) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
        else showMessageDialog(context.getString(R.string.no_internet_connection))
    }

    private suspend fun showMessageDialog(message: String?): Unit = withContext(Dispatchers.Main) {
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