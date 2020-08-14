package az.rabita.lifestep.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.ui.activity.auth.AuthActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

fun Activity.logout() {
    clearToken()
    signOutGoogleAccount()

    startActivity(Intent(this, AuthActivity::class.java))
    finish()
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