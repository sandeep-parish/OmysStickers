package com.omys.stickermaker.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.omys.stickermaker.modal.StickerPackModal

class UserPrefs(context: Context?) {
    private var sharedPref: SharedPreferences? = null
    private val KEY_STICKER_PACK = "sticker_packs"

    init {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getSavedStickerPacks(): ArrayList<StickerPackModal> {
        val stickerPacks = sharedPref?.getString(encrypt(KEY_STICKER_PACK), encrypt(""))
        val arrayTutorialType = object : TypeToken<ArrayList<StickerPackModal>>() {}.type
        return Gson().fromJson(decrypt(stickerPacks), arrayTutorialType) ?: ArrayList()
    }

    fun saveStickerPacks(stickerPacksModal: ArrayList<StickerPackModal>) {
        val edit = sharedPref?.edit()
        val stickerPacks = Gson().toJson(stickerPacksModal)
        edit?.putString(encrypt(KEY_STICKER_PACK), encrypt(stickerPacks))
        edit?.apply()
    }

    //Encrypt and decrypt user data on shared prefrance
    private fun encrypt(input: String): String {
        return Base64.encodeToString(input.toByteArray(), Base64.DEFAULT)
    }

    private fun decrypt(input: String?): String {
        return String(Base64.decode(input, Base64.DEFAULT))
    }

    fun resetPrefs(con: Context) {
        val edit = sharedPref?.edit()
        edit?.putString(KEY_STICKER_PACK, "")
        edit?.clear()
        edit?.apply()
    }
}