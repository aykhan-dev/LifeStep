package az.rabita.lifestep.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import az.rabita.lifestep.network.NetworkState
import az.rabita.lifestep.pojo.apiPOJO.ServerResponsePOJO
import az.rabita.lifestep.pojo.apiPOJO.content.*
import az.rabita.lifestep.pojo.apiPOJO.entity.*
import az.rabita.lifestep.ui.activity.auth.AuthActivity
import retrofit2.Response
import timber.log.Timber
import kotlin.math.pow

fun Activity.restart() {
    val i: Intent? = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
    i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(i)
    finish()
}

fun Activity.logout() {
    val intent = Intent(this, AuthActivity::class.java)
    startActivity(intent)
    finish()
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

fun ContentContentPOJO.asContentEntityObject(groupId: Int) = Content(
    id = id,
    contentKey = transKey,
    content = text,
    imageUrl = url,
    groupID = groupId
)

fun List<ContentContentPOJO>.asContentEntityObject(groupId: Int) = map {
    Content(
        id = it.id,
        contentKey = it.transKey,
        content = it.text,
        imageUrl = it.url,
        groupID = groupId
    )
}

fun List<FriendContentPOJO>.asFriendEntityObject() = map {
    Friend(
        id = it.id,
        surname = it.fullName.split(" ")[0],
        name = it.fullName.split(" ")[1],
        balance = it.balance
    )
}

fun List<CategoriesContentPOJO>.asCategoryEntityObject() = map {
    Category(
        id = it.id,
        name = it.name,
        url = it.url,
        isReady = it.isReady
    )
}

fun List<AssocationContentPOJO>.asAssocationEntityObject() = map {
    Assocation(
        id = it.id,
        name = it.name,
        stepNeed = it.stepNeed.moreShortenString(),
        balance = it.balance.moreShortenString(),
        percent = it.percent,
        url = it.url
    )
}

fun List<WalletContentPOJO>.asWalletEntityObject() = map {
    Wallet(
        id = 1,
        balance = it.balance.toString(),
        balanceMoney = it.balanceMoney.toString(),
        bonusSteps = it.bonusSteps?.littleShortenString(),
        convertSteps = it.convertSteps?.littleShortenString(),
        donationSteps = it.donationSteps?.littleShortenString(),
        transferStepsIn = it.transferStepsIn?.littleShortenString(),
        transferStepsOut = it.transferStepsOut?.littleShortenString()
    )
}

fun List<FriendshipContentPOJO>.asFriendshipStatsEntityObject() = map {
    FriendshipStats(
        id = 1,
        friendsCount = it.friendsCount,
        requestCount = it.requestCount
    )
}

fun List<PersonalInfoContentPOJO>.asPersonalInfoEntityObject() = map {
    PersonalInfo(
        saveId = 1,
        id = it.id,
        name = it.name,
        phone = it.phone,
        invitationCode = it.invitationCode,
        fullName = it.fullName,
        email = it.email,
        url = it.url,
        surname = it.surname,
        createdDate = it.createdDate,
        balance = it.balance,
        friendsCount = it.friendsCount
    )
}

fun List<OwnProfileInfoContentPOJO>.asOwnProfileInfoEntityObject() = map {
    PersonalInfo(
        saveId = 1,
        id = it.id,
        name = it.name,
        phone = it.phone,
        invitationCode = it.invitationCode,
        fullName = it.fullName,
        email = it.email,
        url = it.url,
        surname = it.surname,
        createdDate = it.createdDate,
        balance = it.balance,
        friendsCount = it.friendsCount
    )
}

fun List<DailyContentPOJO>.asDailyStatsEntityObject() = map {
    DailyStat(
        createdDate = it.createdDate,
        count = it.count,
        shortName = it.shortName
    )
}

fun List<MonthlyContentPOJO>.asMonthlyStatsEntityObject() = map {
    MonthlyStat(
        month = it.month,
        count = it.count,
        monthName = it.monthName
    )
}

fun List<NotificationContentPOJO>.asNotificationEntityObject() = map {
    Notification(
        id = it.id,
        imageUrl = it.imageUrl,
        messages = it.messages,
        title = it.title
    )
}

fun List<WeeklyContentPOJO>.asWeeklyStatsEntityObject() = map {
    WeeklyStat(
        date = it.date,
        calories = it.calories,
        convertedSteps = it.convertedSteps,
        isSelected = it.isSelected,
        kilometers = it.kilometers,
        stepCount = it.stepCount,
        unconvertedSteps = it.unconvertedSteps,
        weekName = it.weekName
    )
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