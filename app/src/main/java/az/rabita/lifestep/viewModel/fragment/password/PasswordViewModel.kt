package az.rabita.lifestep.viewModel.fragment.password

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import az.rabita.lifestep.R
import kotlinx.coroutines.launch

class PasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val buttonTexts = listOf(
        application.getString(R.string.show),
        application.getString(R.string.hide)
    )

    private val _textOfButtonShow1 = MutableLiveData<String>().apply { value = buttonTexts[0] }
    val textOfButtonShow1: LiveData<String> get() = _textOfButtonShow1

    private val _textOfButtonShow2 = MutableLiveData<String>().apply { value = buttonTexts[0] }
    val textOfButtonShow2: LiveData<String> get() = _textOfButtonShow2

    fun onShowButton1Click() {
        viewModelScope.launch {
            when (_textOfButtonShow1.value) {
                buttonTexts[0] -> _textOfButtonShow1.postValue(buttonTexts[1])
                buttonTexts[1] -> _textOfButtonShow1.postValue(buttonTexts[0])
                else -> _textOfButtonShow1.postValue(buttonTexts[1])
            }
        }
    }

    fun onShowButton2Click() {
        viewModelScope.launch {
            when (_textOfButtonShow2.value) {
                buttonTexts[0] -> _textOfButtonShow2.postValue(buttonTexts[1])
                buttonTexts[1] -> _textOfButtonShow2.postValue(buttonTexts[0])
                else -> _textOfButtonShow2.postValue(buttonTexts[1])
            }
        }
    }

}