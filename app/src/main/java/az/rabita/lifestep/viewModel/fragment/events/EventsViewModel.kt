package az.rabita.lifestep.viewModel.fragment.events


import android.app.Application
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.pojo.holder.Message
import az.rabita.lifestep.repository.CategoriesRepository
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.isInternetConnectionAvailable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val categoriesRepository = CategoriesRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData(false)
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<Message?>()
    val errorMessage: LiveData<Message?> get() = _errorMessage

    val listOfCategories = categoriesRepository.listOfCategories.asLiveData()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        when (throwable) {
            is NetworkResult.Exceptions.ExpiredToken -> startExpireTokenProcess()
            is NetworkResult.Exceptions.Failure -> handleNetworkException(throwable.message ?: "")
            else -> Timber.e(throwable)
        }
    }

    fun fetchAllCategories() {

        viewModelScope.launch(exceptionHandler) {

            val token = sharedPreferences.token
            val lang = sharedPreferences.langCode

            categoriesRepository.getCategories(token, lang)

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