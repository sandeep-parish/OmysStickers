package com.omys.stickerapp.modal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.omys.stickerapp.database.StickersConverter

/**Created by SandeepParish*/
//this is the original sticker object to be identified by whatsapp while adding sticker

@Entity(tableName = "stickerPacksModal")
class StickerPackModal {

    @PrimaryKey
    var identifier: String = ""

    @TypeConverters(StickersConverter::class)
    var stickers = ArrayList<Sticker>()

    var androidPlayStoreLink: String? = null
    var iosAppStoreLink = ""
    var licenseAgreementWebsite: String? = null
    var name: String? = null
    var privacyPolicyWebsite: String? = null
    var publisher: String? = null
    var publisherEmail: String? = null
    var publisherWebsite: String? = null
    var trayImageFile: String? = null
}

class Sticker {
    var emojis = ArrayList<String>()
    var imageFileName: String? = null
}