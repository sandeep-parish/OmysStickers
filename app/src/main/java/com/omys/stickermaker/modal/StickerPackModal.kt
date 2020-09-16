package com.omys.stickermaker.modal

import android.os.Parcel
import android.os.Parcelable

class StickerPackModal {
    var android_play_store_link: String? = null
    var ios_app_store_link: String? = null
    var sticker_packs = ArrayList<StickerPack>()
}

class StickerPack() : Parcelable {
    var identifier: String? = null
    var image_data_version: String? = null
    var license_agreement_website: String? = null
    var name: String? = null
    var privacy_policy_website: String? = null
    var publisher: String? = null
    var publisher_email: String? = null
    var publisher_website: String? = null
    var stickers = ArrayList<String>()
    var tray_image_file: String? = null
    var createdAt: Long = 0

    constructor(parcel: Parcel) : this() {
        identifier = parcel.readString()
        image_data_version = parcel.readString()
        license_agreement_website = parcel.readString()
        name = parcel.readString()
        privacy_policy_website = parcel.readString()
        publisher = parcel.readString()
        publisher_email = parcel.readString()
        publisher_website = parcel.readString()
        tray_image_file = parcel.readString()
        createdAt = parcel.readLong()
        parcel.readList(stickers as List<*>, null)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(identifier)
        parcel.writeString(image_data_version)
        parcel.writeString(license_agreement_website)
        parcel.writeString(name)
        parcel.writeString(privacy_policy_website)
        parcel.writeString(publisher)
        parcel.writeString(publisher_email)
        parcel.writeString(publisher_website)
        parcel.writeString(tray_image_file)
        parcel.writeLong(createdAt)
        parcel.writeList(stickers as List<*>?)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StickerPack> {
        override fun createFromParcel(parcel: Parcel): StickerPack {
            return StickerPack(parcel)
        }

        override fun newArray(size: Int): Array<StickerPack?> {
            return arrayOfNulls(size)
        }
    }

}/*

class Sticker() : Parcelable {
    var emojis = ArrayList<String>()
    var image_file: String? = null

    constructor(parcel: Parcel) : this() {
        image_file = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(image_file)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sticker> {
        override fun createFromParcel(parcel: Parcel): Sticker {
            return Sticker(parcel)
        }

        override fun newArray(size: Int): Array<Sticker?> {
            return arrayOfNulls(size)
        }
    }
}*/