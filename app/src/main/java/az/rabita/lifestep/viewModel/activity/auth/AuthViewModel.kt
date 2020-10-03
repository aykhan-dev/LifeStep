package az.rabita.lifestep.viewModel.activity.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import az.rabita.lifestep.utils.onOff

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private var _stateFabClick = MutableLiveData<Boolean>().apply { value = false }
    val stateFabClick: LiveData<Boolean> get() = _stateFabClick

    fun onFabClick() = _stateFabClick.onOff()

}