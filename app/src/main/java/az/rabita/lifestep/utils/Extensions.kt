package az.rabita.lifestep.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

fun Context.signOutGoogleAccount() {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    val googleSignInClient = GoogleSignIn.getClient(this, gso)
    googleSignInClient.signOut()
}

fun Context.clearToken() {
    val preferences = PreferenceManager.getInstance(this)
    preferences.setStringElement(TOKEN_KEY, "")
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
    packageManager.getApplicationInfo(packageName, 0)
    true
} catch (exception: PackageManager.NameNotFoundException) {
    false
}