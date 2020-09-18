package com.omys.stickerapp.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.preference.PreferenceManager

object UserPrefs {
    const val KEY_LAST_UPDATED = "last_updated"

    var sp: SharedPreferences? = null
    private fun init(context: Context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getLastUpdated(con: Activity): Long {
        init(con)
        return sp?.getLong(encrypt(KEY_LAST_UPDATED), 0) ?: 0
    }

    fun saveLastUpdated(lastUpdated: Long, con: Activity) {
        init(con)
        val edit = sp?.edit()
        edit?.putLong(encrypt(KEY_LAST_UPDATED), lastUpdated)
        edit?.apply()
    }

    //Encrypt and decrypt user data on sharedprefrance
    private fun encrypt(input: String): String {
        // This is base64 encoding, which is not an encryption
        return Base64.encodeToString(input.toByteArray(), Base64.DEFAULT)
    }

    private fun decrypt(input: String?): String {
        return String(Base64.decode(input, Base64.DEFAULT))
    }

    fun resetPrefs(con: Context) {
        init(con)
        val edit = sp!!.edit()
        edit.putString(KEY_LAST_UPDATED, "")
        edit.apply()
    }
}