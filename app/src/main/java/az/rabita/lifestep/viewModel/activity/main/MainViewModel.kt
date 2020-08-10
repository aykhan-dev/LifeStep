package az.rabita.lifestep.viewModel.activity.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _indexOfSelectedPage = MutableLiveData<Int>()
    val indexOfSelectedPage: LiveData<Int> get() = _indexOfSelectedPage

    private var _isAllowed: Boolean = true
    val isAllowed = _isAllowed

    fun changePage(index: Int) {
        viewModelScope.launch {
            _isAllowed = false
            _indexOfSelectedPage.postValue(index)
        }
    }

}