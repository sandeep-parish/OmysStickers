package com.omys.stickermaker.helpers

import android.app.Activity
import com.omys.stickermaker.modal.Sticker
import com.omys.stickermaker.modal.StickerPackInfoModal
import com.omys.stickermaker.modal.StickerPackModal
import com.omys.stickermaker.utils.*
import java.io.File

class StickerPackHelper(private val activity: Activity) : OnFileDownload {
    private var firebaseHelper: FirebaseHelper? = null
    private var totalFilesToDownload = 0
    private var downloadedFiles = 0

    //Final sticker pack to save in local
    private var stickerPack: StickerPackModal? = null
    private var callback: OnStickerPackCallBack? = null

    init {
        firebaseHelper = FirebaseHelper(activity)
    }

    fun startDownloadingStickerPack(stickerPackInfoModal: StickerPackInfoModal, callback: OnStickerPackCallBack? = null) {
        totalFilesToDownload = stickerPackInfoModal.stickers.size + 1//1 for tray image
        downloadedFiles = 0
        this.callback = callback
        firebaseHelper?.circularProgress?.show()

        with(stickerPackInfoModal) {
            //Download tray Image file
            firebaseHelper?.downloadFileFromFirebaseUrl(tray_image_file,
                    activity.getTrayImagesDirectory(id.toString()),
                    TRAY_IMAGE, this@StickerPackHelper)

            //Download stickers
            stickers.forEach {
                firebaseHelper?.downloadFileFromFirebaseUrl(it,
                        activity.getStickerFilesDirectory(id.toString()), STICKER, this@StickerPackHelper)
            }

            //Create a new original sticker pack to add in whatsapp
            stickerPack = StickerPackModal()
            stickerPack?.identifier = id.toString()
            stickerPack?.name = name
            stickerPack?.publisher = publisher
            stickerPack?.privacyPolicyWebsite = PRIVACY_POLICY
            stickerPack?.licenseAgreementWebsite = PRIVACY_POLICY
            stickerPack?.publisherEmail = PUBLISHER_EMAIL
            stickerPack?.androidPlayStoreLink = ANDROID_PLAY_STORE_LINK
            stickerPack?.publisherWebsite = PUBLISHER_WEBSITE
        }
    }

    override fun onFileDownloaded(file: File?, type: String) {
        downloadedFiles++
        when (type) {
            TRAY_IMAGE -> {
                stickerPack?.trayImageFile = file?.nameWithExtension()
            }
            STICKER -> {
                val sticker = Sticker()
                sticker.imageFileName = file?.nameWithExtension()
                sticker.emojis.add("😀")
                sticker.emojis.add("😺")
                stickerPack?.stickers?.add(sticker)
            }
        }

        if (downloadedFiles >= totalFilesToDownload) {
            firebaseHelper?.circularProgress?.dismiss()
            callback?.onStickerPackDownloaded(stickerPack)//send created sticker pack to it's destination
        }
    }
}

interface OnStickerPackCallBack {
    fun onStickerPackDownloaded(stickerPack: StickerPackModal?)
}