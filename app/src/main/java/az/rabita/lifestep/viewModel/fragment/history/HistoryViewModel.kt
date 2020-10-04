package az.rabita.lifestep.viewModel.fragment.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import az.rabita.lifestep.R
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.pojo.apiPOJO.content.HistoryItemContentPOJO
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.ui.fragment.history.page.HistoryPageType
import az.rabita.lifestep.utils.DEFAULT_LANG
import az.rabita.lifestep.utils.LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import az.rabita.lifestep.utils.isInternetConnectionAvailable

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val transactionsRepository = TransactionsRepository
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchListOfDonations(pageType: HistoryPageType): LiveData<PagingData<HistoryItemContentPOJO>> {
        val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
        val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)
        return transactionsRepository.getHistoryDonationsListStream(
            token = token,
            lang = lang,
            pageType = pageType,
            onErrorListener = { handleNetworkException(it) },
            onExpireTokenListener = { startExpireTokenProcess() }
        ).cachedIn(viewModelScope)
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
        if (_eventExpiredToken.value == false) _eventExpiredToken.postValue(true)
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}