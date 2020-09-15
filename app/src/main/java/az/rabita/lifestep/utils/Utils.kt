package az.rabita.lifestep.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.ServerResponsePOJO
import az.rabita.lifestep.pojo.apiPOJO.content.AdsTransactionContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.DailyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.MonthlyContentPOJO
import az.rabita.lifestep.pojo.dataHolder.AdsTransactionInfoHolder
import az.rabita.lifestep.ui.activity.auth.AuthActivity
import az.rabita.lifestep.ui.custom.BarDiagram
import retrofit2.Response
import timber.log.Timber
import java.util.*
import kotlin.math.pow

fun Activity.restart() {
    val i: Intent? = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
    i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(i)
    finish()
}

fun Fragment.openUrl(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

fun Activity.openUrl(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

fun Fragment.callNumber(number: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$number")
    startActivity(intent)
}

fun pxFromDp(context: Context, dp: Float): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}

fun MutableLiveData<Boolean>.onOff() {
    value = true
    value = false
}

fun View.makeInvisible() {
    visibility = INVISIBLE
}

fun View.makeVisible() {
    visibility = VISIBLE
}

fun Context.toast(message: String?) {
    Toast.makeText(this, message ?: NO_MESSAGE, Toast.LENGTH_SHORT).show()
    Timber.e(message ?: NO_MESSAGE)
}

fun isEmailValid(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isPinValid(pin: String): Boolean {
    return pin.length == 4 && pin.isDigitsOnly()
}

fun isPasswordValid(password: String): Boolean {
    return password.length >= 6
}

fun View.hideKeyboard(context: Context?) {
    val inputMethodManager =
        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}

fun getDateAndTime(): String {
    val date = Calendar.getInstance().time
    return DateFormat.format("yyyy-MM-dd hh:mm:ss", date).toString()
}

fun Context.isInternetConnectionAvailable(): Boolean {
    var result = false
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = cm.activeNetwork ?: return false
        val actNw =
            cm.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        cm.run {
            cm.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }

            }
        }
    }
    return result
}

fun Long.littleShortenString(): String {
    return when {
        this >= 10.0.pow(9.0) -> "${this / 10.0.pow(9.0).toInt()} mly"
        this >= 10.0.pow(6.0) -> "${this / 10.0.pow(6.0).toInt()} mln"
        else -> "$this"
    }
}

fun Long.moreShortenString(): String {
    return when {
        this >= 10.0.pow(9.0) -> "${this / 10.0.pow(9.0).toInt()} mly"
        this >= 10.0.pow(6.0) -> "${this / 10.0.pow(6.0).toInt()} mln"
        this >= 1000 -> "${this / 1000} min"
        else -> "$this"
    }
}

fun AdsTransactionContentPOJO.asAdsTransactionInfoHolderObject(): AdsTransactionInfoHolder {
    return AdsTransactionInfoHolder(
        transactionId,
        videoUrl,
        description,
        logoUrl,
        title,
        subTitle,
        url,
        time
    )
}

fun MutableLiveData<*>.notifyObservers() {
    value = value
}

fun <T> checkNetworkRequestResponse(response: Response<ServerResponsePOJO<T>>): NetworkState {
    return if (response.isSuccessful && response.code() == 200) {
        response.body()?.let { data ->
            when (data.status.code) {
                200, 201 -> NetworkState.Success(data.content)
                300 -> NetworkState.ExpiredToken
                else -> NetworkState.HandledHttpError(data.status.text)
            }
        } ?: NetworkState.InvalidData
    } else NetworkState.UnhandledHttpError(response.message() + response.code())
}

fun extractDiagramData(data: List<*>): BarDiagram.DiagramDataModel {
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

    return BarDiagram.DiagramDataModel(
        maxValue = maxValue,
        columnTexts = columns,
        values = values
    )
}