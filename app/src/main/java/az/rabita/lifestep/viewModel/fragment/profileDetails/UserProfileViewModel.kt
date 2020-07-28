package az.rabita.lifestep.viewModel.fragment.profileDetails

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.*
import az.rabita.lifestep.R
import az.rabita.lifestep.local.getDatabase
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.content.DailyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.MonthlyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.PersonalInfoContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.FriendRequestModelPOJO
import az.rabita.lifestep.pojo.apiPOJO.model.SendStepModelPOJO
import az.rabita.lifestep.repository.FriendshipRepository
import az.rabita.lifestep.repository.ReportRepository
import az.rabita.lifestep.repository.TransactionsRepository
import az.rabita.lifestep.repository.UsersRepository
import az.rabita.lifestep.ui.custom.BarDiagram
import az.rabita.lifestep.ui.fragment.userProfile.FriendshipStatus
import az.rabita.lifestep.utils.*
import kotlinx.coroutines.launch
import java.util.*

@Suppress("UNCHECKED_CAST")
class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPreferences = PreferenceManager.getInstance(context)

    private val usersRepository = UsersRepository.getInstance(getDatabase(context))
    private val reportRepository = ReportRepository.getInstance(getDatabase(context))
    private val transactionsRepository = TransactionsRepository
    private val friendshipRepository = FriendshipRepository.getInstance(
        PaginationListeners(
            {},
            {},
            { handleNetworkException(it) }
        )
    )

    private val _eventExpiredToken = MutableLiveData<Boolean>().apply { value = false }
    val eventExpiredToken: LiveData<Boolean> get() = _eventExpiredToken

    private val _eventDismissDialog = MutableLiveData<Boolean>()
    val eventDismissDialog: LiveData<Boolean> get() = _eventDismissDialog

    private val _eventShowSendStepsDialog = MutableLiveData<Boolean>()
    val eventShowSendStepsDialog: LiveData<Boolean> get() = _eventShowSendStepsDialog

    private val _dailyTextSelected = MutableLiveData<Boolean>().apply { value = true }
    val dailyTextSelected: LiveData<Boolean> get() = _dailyTextSelected

    private val _monthlyTextSelected = MutableLiveData<Boolean>().apply { value = false }
    val monthlyTextSelected: LiveData<Boolean> get() = _monthlyTextSelected

    private val _stats = MutableLiveData<BarDiagram.DiagramDataModel>()
    val stats: LiveData<BarDiagram.DiagramDataModel> get() = _stats

    private val _profileInfo = MutableLiveData<PersonalInfoContentPOJO>()
    val profileInfo: LiveData<PersonalInfoContentPOJO> get() = _profileInfo

    private var _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    val friendshipStatus: LiveData<FriendshipStatus> = MutableLiveData()

    val personalInfo = usersRepository.personalInfo.asLiveData()

    val sendStepInput = MutableLiveData<String>().apply {
        observeForever {
            val input = if (value?.isEmpty() != false) 0 else (value ?: "0").toLong()
            if (input > personalInfo.value?.balance ?: 0) postValue("${personalInfo.value?.balance ?: 0}")
        }
    }

    fun fetchPersonalInfo() = viewModelScope.launch {

        val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
        val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

        when (val response = usersRepository.getPersonalInfo(token, lang)) {
            is NetworkState.Success<*> -> _eventShowSendStepsDialog.onOff()
            is NetworkState.ExpiredToken -> startExpireTokenProcess()
            is NetworkState.HandledHttpError -> showMessageDialog(response.error)
            is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
            is NetworkState.NetworkException -> handleNetworkException(response.exception)
        }

    }

    fun fetchUserProfileDetails(userId: String) = viewModelScope.launch {

        val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
        val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

        when (val response = usersRepository.getUserInfoById(token, lang, userId)) {
            is NetworkState.Success<*> -> {
                val data = response.data as List<PersonalInfoContentPOJO>
                _profileInfo.value = data[0].also {
                    (friendshipStatus as MutableLiveData).postValue(
                        when (it.friendShipStatus) {
                            0 -> FriendshipStatus.NOT_FRIEND
                            10 -> FriendshipStatus.PENDING
                            20 -> FriendshipStatus.IS_FRIEND
                            //30 -> FriendshipStatus.REJECTED
                            else -> FriendshipStatus.NOT_FRIEND
                        }
                    )
                }
                refreshStats()
            }
            is NetworkState.ExpiredToken -> startExpireTokenProcess()
            is NetworkState.HandledHttpError -> showMessageDialog(response.error)
            is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
            is NetworkState.NetworkException -> handleNetworkException(response.exception)
        }

    }

    fun sendFriendRequest() {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)
            val model = FriendRequestModelPOJO(friendId = profileInfo.value?.id ?: "")

            when (val response = friendshipRepository.sendFriendRequest(token, lang, model)) {
                is NetworkState.Success<*> -> fetchUserProfileDetails(profileInfo.value?.id ?: "")
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    fun sendStep(userId: String) {
        viewModelScope.launch {

            val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
            val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

            val date = Calendar.getInstance().time
            val formatted = DateFormat.format("yyyy-MM-dd hh:mm:ss", date)

            val model = SendStepModelPOJO(
                userId = userId,
                count = (sendStepInput.value ?: "0").toLong(),
                date = formatted.toString()
            )

            when (val response = transactionsRepository.transferSteps(token, lang, model)) {
                is NetworkState.Success<*> -> _eventDismissDialog.onOff()
                is NetworkState.ExpiredToken -> startExpireTokenProcess()
                is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                is NetworkState.NetworkException -> handleNetworkException(response.exception)
            }

        }
    }

    fun onDailyTextClick() {
        _dailyTextSelected.value = true
        _monthlyTextSelected.value = false
        refreshStats()
    }

    fun onMonthlyTextClick() {
        _monthlyTextSelected.value = true
        _dailyTextSelected.value = false
        refreshStats()
    }

    private fun refreshStats() {
        if (_dailyTextSelected.value!!) refreshDailyStats()
        if (_monthlyTextSelected.value!!) refreshMonthlyStats()
    }

    private fun refreshDailyStats() {
        viewModelScope.launch {

            profileInfo.value?.let { info ->

                val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
                val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

                when (val response =
                    reportRepository.getDailyStats(token, lang, info.id)) {
                    is NetworkState.Success<*> -> run {
                        val data = response.data as List<DailyContentPOJO>
                        extractDiagramData(data)
                    }
                    is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                    is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                    is NetworkState.NetworkException -> handleNetworkException(response.exception)
                }

            }

        }
    }

    private fun refreshMonthlyStats() {
        viewModelScope.launch {

            profileInfo.value?.let { info ->

                val token = sharedPreferences.getStringElement(TOKEN_KEY, "")
                val lang = sharedPreferences.getIntegerElement(LANG_KEY, DEFAULT_LANG)

                when (val response =
                    reportRepository.getMonthlyStats(token, lang, info.id)) {
                    is NetworkState.Success<*> -> run {
                        val data = response.data as List<MonthlyContentPOJO>
                        extractDiagramData(data)
                    }
                    is NetworkState.UnhandledHttpError -> showMessageDialog(response.error)
                    is NetworkState.HandledHttpError -> showMessageDialog(response.error)
                    is NetworkState.NetworkException -> handleNetworkException(response.exception)
                }

            }

        }
    }

    private fun extractDiagramData(data: List<*>) {
        var maxValue = 0L
        val columns = mutableListOf<String>()
        val values = mutableListOf<Long>()

        for (i in data) {
            when (i) {
                is MonthlyContentPOJO -> {
                    maxValue = maxValue.coerceAtLeast(i.count)
                    columns.add(i.monthName)
                    values.add(i.count)
                }
                is DailyContentPOJO -> {
                    maxValue = maxValue.coerceAtLeast(i.count)
                    columns.add(i.createdDate.substring(8, 10))
                    values.add(i.count)
                }
            }
        }

        _stats.value = BarDiagram.DiagramDataModel(
            maxValue = maxValue,
            columnTexts = columns,
            values = values
        )
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