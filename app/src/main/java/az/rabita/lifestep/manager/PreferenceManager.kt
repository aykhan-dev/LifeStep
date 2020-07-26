package az.rabita.lifestep.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import az.rabita.lifestep.R

class PreferenceManager private constructor(val context: Context) {

    companion object : SingletonHolder<PreferenceManager, Context>(::PreferenceManager)

    private var sharedPreferences: SharedPreferences? = null

    init {
        sharedPreferences = context.getSharedPreferences(
            context.resources.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
    }

    fun setStringElement(key: String, value: String) {
        with(sharedPreferences?.edit()) {
            this?.putString(key, value)
            this?.apply()
        }
    }

    fun getStringElement(key: String, defaultValue: String): String {
        return sharedPreferences?.getString(key, defaultValue) ?: ""
    }

    fun setIntegerElement(key: String, value: Int) {
        with(sharedPreferences?.edit()) {
            this?.putInt(key, value)
            this?.apply()
        }
    }

    fun getIntegerElement(key: String, defaultValue: Int): Int {
        return sharedPreferences?.getInt(key, defaultValue) ?: 0
    }

    fun setBooleanElement(key: String, value: Boolean) {
        with(sharedPreferences?.edit()) {
            this?.putBoolean(key, value)
            this?.apply()
        }
    }

    fun getBooleanElement(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences?.getBoolean(key, defaultValue) ?: false
    }

}

