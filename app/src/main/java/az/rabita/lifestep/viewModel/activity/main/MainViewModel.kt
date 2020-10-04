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

    fun changePage(index: Int) {
        viewModelScope.launch {
            _indexOfSelectedPage.postValue(index)
        }
    }

}