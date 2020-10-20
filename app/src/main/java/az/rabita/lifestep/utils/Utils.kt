package az.rabita.lifestep.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import az.rabita.lifestep.network.NetworkResult
import az.rabita.lifestep.network.NetworkResultFailureType
import az.rabita.lifestep.pojo.apiPOJO.ServerResponsePOJO
import az.rabita.lifestep.pojo.apiPOJO.content.AdsTransactionContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.DailyContentPOJO
import az.rabita.lifestep.pojo.apiPOJO.content.MonthlyContentPOJO
import az.rabita.lifestep.pojo.dataHolder.AdsTransactionInfoHolder
import az.rabita.lifestep.ui.custom.BarDiagram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.pow

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

fun View.hideKeyboard() {
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

suspend fun extractDiagramData(data: List<*>): BarDiagram.DiagramDataModel =
    withContext(Dispatchers.Default) {
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
                    columns.add(i.shortName)
                    values.add(i.count)
                }
            }
        }

        return@withContext BarDiagram.DiagramDataModel(
            maxValue = if (maxValue == 0L) 10 else maxValue,
            columnTexts = columns,
            values = values
        )
    }

fun Context.getBitmapFromUri(uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else MediaStore.Images.Media.getBitmap(contentResolver, uri)
}

fun Bitmap.toByteArray(compressFormat: Bitmap.CompressFormat, quality: Int): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(compressFormat, quality, stream)
    return stream.toByteArray()
}

fun Context.convertByteArrayToFile(byteArray: ByteArray, path: String): File? {
    return try {
        val file = File(cacheDir, path)
        with(FileOutputStream(file)) {
            write(byteArray)
            flush()
            close()
        }
        file
    } catch (exc: Exception) {
        Timber.e(exc)
        null
    }
}

fun Activity.makeEdgeToEdge() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(true)
    } else {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}

suspend fun <T> networkRequest(request: suspend () -> Response<ServerResponsePOJO<T>>): NetworkResult =
    withContext(Dispatchers.IO) {
        try {
            val response = request()
            checkNetworkRequestResponse(response)
        } catch (exp: Exception) {
            NetworkResult.Failure(NetworkResultFailureType.ERROR, exp.message ?: "")
        }
    }

fun <T> checkNetworkRequestResponse(response: Response<ServerResponsePOJO<T>>): NetworkResult {
    return if (response.isSuccessful && response.code() == 200) {
        response.body()?.let { body ->
            when (val code = body.status.code) {
                200, 201 -> NetworkResult.Success(body.content)
                else -> NetworkResult.Failure(
                    if (code == 300) NetworkResultFailureType.EXPIRED_TOKEN else NetworkResultFailureType.ERROR,
                    body.status.text
                )
            }
        } ?: NetworkResult.Failure(NetworkResultFailureType.ERROR, "")
    } else NetworkResult.Failure(NetworkResultFailureType.ERROR, response.message())
}
