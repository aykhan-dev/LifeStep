package az.rabita.lifestep.manager

import android.content.Context
import android.content.SharedPreferences
import az.rabita.lifestep.R

class PreferenceManager private constructor(val context: Context) {

    companion object : SingletonHolder<PreferenceManager, Context>(::PreferenceManager) {
        const val LANG_CODE_KEY_WORD = "language code"
        const val LANG_AZ_KEY_WORD = "language az"
        const val LANG_EN_KEY_WORD = "language en"
        const val LANG_RU_KEY_WORD = "language ru"

        const val LANG_CODE_AZ = 10
        const val LANG_CODE_EN = 20
        const val LANG_CODE_RU = 30

        const val DEFAULT_LANG_CODE = LANG_CODE_AZ

        const val LANG_INDEX_AZ = "az"
        const val LANG_INDEX_EN = "en"
        const val LANG_INDEX_RU = "ru"

        const val DEFAULT_LANG_INDEX = LANG_INDEX_AZ

        const val USER_TOKEN_KEY_WORD = "user access token"
        const val DEFAULT_TOKEN = ""
    }

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(
        context.resources.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    var langCode
        get() = getIntegerElement(LANG_CODE_KEY_WORD, DEFAULT_LANG_CODE)
        set(value) = setIntegerElement(LANG_CODE_KEY_WORD, value)

    val language: String
        get() = when (langCode) {
            LANG_CODE_AZ -> LANG_INDEX_AZ
            LANG_CODE_EN -> LANG_INDEX_EN
            LANG_CODE_RU -> LANG_INDEX_RU
            else -> DEFAULT_LANG_INDEX
        }

    var token
        get() = getStringElement(USER_TOKEN_KEY_WORD, DEFAULT_TOKEN)
        set(value) = setStringElement(USER_TOKEN_KEY_WORD, value)

    fun setStringElement(key: String, value: String) {
        with(sharedPreferences.edit()) {
            this?.putString(key, value)
            this?.apply()
        }
    }

    fun getStringElement(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: ""
    }

    private fun setIntegerElement(key: String, value: Int) {
        with(sharedPreferences.edit()) {
            this?.putInt(key, value)
            this?.apply()
        }
    }

    private fun getIntegerElement(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

}

