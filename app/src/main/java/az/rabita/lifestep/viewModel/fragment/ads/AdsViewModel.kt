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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("UNCHECKED_CAST")
class AdsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val contentsRepository = ContentsRepository.getInstance(getDatabase(context))
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

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (uiState.value == UiState.Loading) uiState.value = UiState.LoadingFinished
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun fetchAdsPageContent() {

        viewModelScope.launch(exceptionHandler) {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            contentsRepository.getContent(token, lang, ADS_GROUP_ID)

        }

    }

    fun getBonusSteps(data: Map<String, Any>) {

        val transactionId = data[AdsDialog.ID_KEY] as String
        val isForBonusSteps = data[AdsDialog.IS_FOR_BONUS_STEPS_KEY] as Boolean
        val watchTime = data[AdsDialog.TOTAL_WATCH_TIME_KEY] as Int

        if (!isForBonusSteps) return

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = ConvertStepsModelPOJO(
                transactionId = transactionId,
                watchTime = watchTime,
                createdDate = getDateAndTime()
            )

            adsRepository.sendBonusAdsTransactionResult(token, lang, model)

            _eventShowCongratsDialog.onOff()

            uiState.value = UiState.LoadingFinished

        }

    }

    fun createAdsTransaction() {

        viewModelScope.launch(exceptionHandler) {

            uiState.value = UiState.Loading

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            val model = DateModelPOJO(date = getDateAndTime())

            val response = adsRepository.createAdsTransaction(token, lang, model)

            val data = (response as NetworkResult.Success<List<AdsTransactionContentPOJO>>).data
            if (data.isNotEmpty()) _adsTransaction.postValue(data[0])

            uiState.value = UiState.LoadingFinished

        }

    }

    private fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
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