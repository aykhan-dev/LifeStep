package az.rabita.lifestep.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.fragment.app.Fragment
import az.rabita.lifestep.R
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.ui.activity.auth.AuthActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.text.DecimalFormat

fun Activity.logout() {
    if (this::class.java.name == "az.rabita.lifestep.ui.activity.main.MainActivity") {
        clearToken()
        signOutGoogleAccount()

        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}

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

fun Context.signOutGoogleAccount() {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    val googleSignInClient = GoogleSignIn.getClient(this, gso)
    googleSignInClient.signOut()
}

fun Context.clearToken() {
    val preferences = PreferenceManager.getInstance(this)
    preferences.token = ""
}

fun Context.shortenString(number: Long, minimalLength: Int = 3): String {
    val suffix = resources.getStringArray(R.array.calcalationSuffixes)
    val df = DecimalFormat("###.#")
    return if (number.toString().length > minimalLength) {
        when (number) {
            in 1000..999999 -> "${df.format(number / 1000f)}${suffix[0]}"
            in 1000000..999999999 -> "${df.format(number / 1000000f)}${suffix[1]}"
            in 1000000000..999999999999 -> "${df.format(number / 1000000000f)}${suffix[2]}"
            else -> number.toString()
        }
    } else number.toString()
}

fun Context.appIsExist(packageName: String): Boolean = try {
    packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    true
} catch (exception: Exception) {
    false
}