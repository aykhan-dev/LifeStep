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
import az.rabita.lifestep.pojo.apiPOJO.model.DateModelPOJO
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.AdsRepository
import az.rabita.lifestep.repository.ContentsRepository
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.ui.dialog.ads.AdsDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class AdsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val contentsRepository = ContentsRepository.getInstance(getDatabase(context))
    private val transactionsRepository = TransactionsRepository
    private val adsRepository = AdsRepository

    private val _adsTransaction = MutableLiveData<AdsTransactionContentPOJO>()
    val adsTransaction: LiveData<AdsTransactionContentPOJO> get() = _adsTransaction

    private val _eventShowCongratsDialog = MutableLiveData<Boolean>()
    val eventShowCongratsDialog: LiveData<Boolean> = _eventShowCongratsDialog

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    val title = contentsRepository.adsContentTitle
    val body = contentsRepository.adsContentBody

    val uiState = MutableLiveData<UiState?>()

    fun fetchAdsPageContent() {
        viewModelScope.launch {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            when (val response = contentsRepository.getContent(token, lang, ADS_GROUP_ID)) {
                is NetworkResult.Failure -> when (response.type) {
                    NetworkResultFailureType.EXPIRED_TOKEN -> startExpireTokenProcess()
                    else -> handleNetworkException(response.message)
                }
            }

        }
    }

    fun getBonusSteps(data: Map<String, Any>) {

        val transactionId = data[AdsDialog.ID_KEY] as String
        val isForBonusSteps = data[AdsDialog.IS_FOR_BONUS_STEPS_KEY] as Boolean
        val watchTime = data[AdsDialog.TOTAL_WATCH_TIME_KEY] as Int

        if (!isForBonusSteps) return

        viewModelScope.launch {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = ConvertStepsModelPOJO(
                transactionId = transactionId,
                watchTime = watchTime,
                createdDate = getDateAndTime()
            )

            when (val response = adsRepository.sendBonusAdsTransactionResult(token, lang, model)) {
                is NetworkResult.Success<*> -> _eventShowCongratsDialog.onOff()
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
                        _adsTransaction.postValue(data[0])
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

    private suspend fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(
            context.getString(R.string.no_internet_connection),
            MessageType.NO_INTERNET
        )
    }

    private suspend fun showMessageDialog(message: String, type: MessageType): Unit =
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

}