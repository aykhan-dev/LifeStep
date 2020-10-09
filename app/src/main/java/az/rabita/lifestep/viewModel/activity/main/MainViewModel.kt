package az.rabita.lifestep.viewModel.activity.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _indexOfSelectedPage = MutableLiveData(2)
    val indexOfSelectedPage: LiveData<Int> = _indexOfSelectedPage

    fun changePage(index: Int) {
        _indexOfSelectedPage.postValue(index)
    }

}