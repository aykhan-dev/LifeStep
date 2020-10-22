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
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.ui.fragment.history.page.HistoryPageType


import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val transactionsRepository = TransactionsRepository
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    fun fetchListOfDonations(pageType: HistoryPageType): LiveData<PagingData<HistoryItemContentPOJO>> {
        val token = sharedPreferences.token
        val lang = sharedPreferences.langCode
        return transactionsRepository.getHistoryDonationsListStream(
            token = token,
            lang = lang,
            pageType = pageType,
            onExpireTokenListener = { withContext(Dispatchers.Main) { startExpireTokenProcess() } },
            onErrorListener = { withContext(Dispatchers.Main) { handleNetworkException(it) } }
        ).cachedIn(viewModelScope)
    }

    private fun handleNetworkException(exception: String) {
        if (context.isInternetConnectionAvailable()) showMessageDialog(exception, MessageType.ERROR)
        else showMessageDialog(
            context.getString(R.string.no_internet_connection),
            MessageType.ERROR
        )
    }

    private fun showMessageDialog(message: String, type: MessageType) {
        _errorMessage.value = Message(message, type)
        _errorMessage.value = null
    }

    private fun startExpireTokenProcess() {
        sharedPreferences.token = ""
        if (_eventExpiredToken.value == false) _eventExpiredToken.postValue(true)
    }

    fun endExpireTokenProcess() {
        _eventExpiredToken.value = false
    }

}