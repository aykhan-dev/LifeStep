package az.rabita.lifestep.viewModel.fragment.wallet

import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.utils.DEFAULT_LANG
import az.rabita.lifestep.utils.LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.launch

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val reportRepository = ReportRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val walletInfo = reportRepository.walletInfo.asLiveData()

    fun fetchWalletInfo() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getTotalTransactionInfo(token, lang)) {
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
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