package com.omys.stickerapp.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.omys.stickerapp.modal.Sticker

class StickersConverter {
    @TypeConverter
    fun listToSimpleJson(stickers: ArrayList<Sticker>): String {
        val type = object : TypeToken<ArrayList<Sticker>>() {}.type
        return Gson().toJson(stickers, type)
    }

    @TypeConverter
    fun toStickersList(value: String): ArrayList<Sticker> {
        val type = object : TypeToken<ArrayList<Sticker>>() {}.type
        return Gson().fromJson(value, type)
    }

    //FirebaseModal Convertor
    @TypeConverter
    fun firebaseListToJson(stickers: ArrayList<String>): String {
        val type = object : TypeToken<ArrayList<Sticker>>() {}.type
        return Gson().toJson(stickers, type)
    }

    @TypeConverter
    fun toStickerListFromFirebaseJson(value: String): ArrayList<String> {
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(value, type)
    }
}