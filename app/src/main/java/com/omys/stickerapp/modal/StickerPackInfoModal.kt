package com.omys.stickerapp.modal

import android.os.Parcel
import android.os.Parcelable

/**Created By SandeepParish
 *  on 16/09/2020
 * */
class StickerPackInfoModal() : Parcelable {
    var id: String? = null
    var name: String? = null
    var publisher: String? = null
    var stickers = ArrayList<String>()
    var tray_image_file: String? = null
    var createdAt: Long = 0
    var totalStickers: Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        name = parcel.readString()
        publisher = parcel.readString()
        tray_image_file = parcel.readString()
        createdAt = parcel.readLong()
        totalStickers = parcel.readInt()
        parcel.readList(stickers as List<*>, null)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(publisher)
        parcel.writeString(tray_image_file)
        parcel.writeLong(createdAt)
        parcel.writeInt(totalStickers)
        parcel.writeList(stickers as List<*>?)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StickerPackInfoModal> {
        override fun createFromParcel(parcel: Parcel): StickerPackInfoModal {
            return StickerPackInfoModal(parcel)
        }

        override fun newArray(size: Int): Array<StickerPackInfoModal?> {
            return arrayOfNulls(size)
        }
    }

}