package com.omys.stickermaker.modal

/**Created by SandeepParish*/
//this is the original sticker object to be identified by whatsapp while adding sticker
class StickerPackModal {
    var androidPlayStoreLink: String? = null
    var iosAppStoreLink = ""
    var identifier: String? = null
    var isWhitelisted: Boolean? = null
    var licenseAgreementWebsite: String? = null
    var name: String? = null
    var privacyPolicyWebsite: String? = null
    var publisher: String? = null
    var publisherEmail: String? = null
    var publisherWebsite: String? = null
    var stickers = ArrayList<Sticker>()
    var totalSize: Int? = null
    var trayImageFile: String? = null
}

class Sticker {
    var emojis = ArrayList<String>()
    var imageFileName: String? = null
}