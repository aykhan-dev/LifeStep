package az.rabita.lifestep.viewModel.fragment.ranking

import android.app.Application
import az.rabita.lifestep.utils.DEFAULT_LANG
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.utils.LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class RankingViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val reportRepository = ReportRepository.getInstance(getDatabase(context))

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _listOfChampions = MutableLiveData<List<RankerContentPOJO>>()
    val listOfChampions: LiveData<List<RankerContentPOJO>> get() = _listOfChampions

    fun fetchAllDonors(id: String): LiveData<PagingData<RankerContentPOJO>> {
        val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
         val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)
        return usersRepository.getDonorsListStream(
            token,
            lang,
            id,
            { showMessageDialog(it) },
            { startExpireTokenProcess() }
        ).cachedIn(viewModelScope)
    }

    fun fetchAllChampions() {
        viewModelScope.launch {
            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            when (val response = reportRepository.getChampionsOfWeek(token, lang)) {
                is NetworkState.Success<*> -> run {
                    val data = response.data as List<RankerContentPOJO>
                    _listOfChampions.value = data
                }
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> showMessageDialog(response.exception)
            }
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