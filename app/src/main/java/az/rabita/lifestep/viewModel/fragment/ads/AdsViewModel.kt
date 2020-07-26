package az.rabita.lifestep.viewModel.fragment.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.AdsTransactionContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.DateModelPOJO
import az.rabita.lifestep.repository.AdsRepository
import az.rabita.lifestep.repository.ContentsRepository
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class AdsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val contentsRepository = ContentsRepository.getInstance(getDatabase(context))
    private val transactionsRepository = TransactionsRepository
    private val adsRepository = AdsRepository

    private val _adsTransaction = MutableLiveData<AdsTransactionContentPOJO>()
    val adsTransaction: LiveData<AdsTransactionContentPOJO> get() = _adsTransaction

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val title = contentsRepository.adsContentTitle
    val body = contentsRepository.adsContentBody

    val uiState = MutableLiveData<UiState>()

    fun fetchAdsPageContent() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = contentsRepository.getContent(token, lang, ADS_GROUP_ID)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    fun getBonusSteps() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val model = DateModelPOJO(date = getDateAndTime())

            when (val response = transactionsRepository.getBonusSteps(token, lang, model)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    fun createAdsTransaction() {
        viewModelScope.launch {

            uiState.value = (UiState.Loading)

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, 10)

            val model = DateModelPOJO(date = getDateAndTime())

            when (val response = adsRepository.createAdsTransaction(token, lang, model)) {
                is NetworkState.Success<*> -> {
                    val data = response.data as List<AdsTransactionContentPOJO>
                    if (data.isNotEmpty()) _adsTransaction.value = data[0]
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

            uiState.value = (UiState.LoadingFinished)

        }
    }

    private fun handleNetworkException(exception: String?) {
        viewModelScope.launch {
            if (context.isInternetConnectionAvailable()) showMessageDialog(exception)
            else showMessageDialog(NO_INTERNET_CONNECTION)
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