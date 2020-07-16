package az.rabita.lifestep.viewModel.activity.auth

import android.app.Application
import az.rabita.lifestep.utils.DEFAULT_LANG
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import az.rabita.lifestep.utils.onOff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private var _stateFabClick = MutableLiveData<Boolean>().apply { value = false }
    val stateFabClick: LiveData<Boolean> get() = _stateFabClick

    fun onFabClick() = _stateFabClick.onOff()

}