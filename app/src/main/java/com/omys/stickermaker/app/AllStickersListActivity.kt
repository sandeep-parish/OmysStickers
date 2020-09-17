package com.omys.stickermaker.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.omys.stickermaker.R
import com.omys.stickermaker.adapter.StickerPacksListAdapter
import com.omys.stickermaker.helpers.FirebaseHelper
import com.omys.stickermaker.helpers.OnFileDownload
import com.omys.stickermaker.modal.Sticker
import com.omys.stickermaker.modal.StickerPackInfoModal
import com.omys.stickermaker.modal.StickerPackModal
import com.omys.stickermaker.utils.*
import kotlinx.android.synthetic.main.activity_all_stickers_list.*
import java.io.File

class AllStickersListActivity : AppCompatActivity(), StickerPacksListAdapter.OnStickerPackAction, OnFileDownload {

    private var totalFilesToDownload = 0
    private var downloadedFiles = 0

    private var stickerPackModal: StickerPackModal? = null
    private var allDownloadedStickerPacks = ArrayList<StickerPackModal>()

    private val userPref by lazy { UserPrefs(this) }
    private var firebaseHelper: FirebaseHelper? = null
    private val stickerPacksAdapter by lazy { StickerPacksListAdapter(this) }
    private var firestore: FirebaseFirestore? = null
    private var collectionReference: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_stickers_list)

        bindUiViews()
    }

    private fun bindUiViews() {
        firestore = FirebaseFirestore.getInstance()
        collectionReference = firestore?.collection(KEY_STICKER_PACK)
        firebaseHelper = FirebaseHelper(this)

        allDownloadedStickerPacks.addAll(userPref.getSavedStickerPacks())

        stickerPackList.adapter = stickerPacksAdapter
        fabAddNewStickers.setOnClickListener {
            it.avoidDoubleClicks()
            startCreateNewSticker()
        }

        getAllAvailableStickers()
    }

    /**Get sticker packs from firestore */
    private fun getAllAvailableStickers() {
        firebaseHelper?.progressDialog?.show()
        val query = collectionReference?.orderBy(KEY_CREATED_AT, Query.Direction.DESCENDING)
        query?.get()?.addOnCompleteListener { task ->
            firebaseHelper?.progressDialog?.dismiss()
            if (task.isComplete && task.isSuccessful) {
                val stickerPacks = ArrayList<StickerPackInfoModal>()
                task.result?.forEach {
                    val sticker = it.toObject(StickerPackInfoModal::class.java)
                    stickerPacks.add(sticker)
                }
                stickerPacksAdapter.setStickerPacks(stickerPacks)
            }
        }?.addOnFailureListener {
            firebaseHelper?.progressDialog?.dismiss()
        }
    }

    override fun onStickerPackDownload(stickerPackInfoModal: StickerPackInfoModal) {
        stickerPackModal = StickerPackModal()//Create new sticker pack while download to save in local

        with(stickerPackInfoModal) {
            stickerPackModal?.identifier = id
            stickerPackModal?.name = name
            stickerPackModal?.publisher = publisher
            stickerPackModal?.privacyPolicyWebsite = PRIVACY_POLICY
            stickerPackModal?.licenseAgreementWebsite = PRIVACY_POLICY
            stickerPackModal?.publisherEmail = PUBLISHER_EMAIL
            stickerPackModal?.androidPlayStoreLink = ANDROID_PLAY_STORE_LINK
            stickerPackModal?.publisherWebsite = PUBLISHER_WEBSITE

            totalFilesToDownload += stickers.size + 1

            firebaseHelper?.downloadFileFromFirebaseUrl(tray_image_file,
                    getTrayImagesDirectory(id.toString()), TRAY_IMAGE, this@AllStickersListActivity)

            stickerPackInfoModal.stickers.forEach {
                firebaseHelper?.downloadFileFromFirebaseUrl(it,
                        getStickerFilesDirectory(id.toString()), STICKER, this@AllStickersListActivity)
            }

            firebaseHelper?.circularProgress?.show()
        }
    }

    override fun onFileDownloaded(file: File?, type: String) {
        downloadedFiles++
        val fileName = file?.absolutePath?.substring(file.absolutePath.lastIndexOf("/") + 1)
        when (type) {
            TRAY_IMAGE -> {
                stickerPackModal?.trayImageFile = fileName
            }
            STICKER -> {
                val sticker = Sticker()
                sticker.imageFileName = fileName
                stickerPackModal?.stickers?.add(sticker)
            }
        }
        if (downloadedFiles >= totalFilesToDownload) {
            firebaseHelper?.circularProgress?.dismiss()
            saveDownloadedDataToLocal()
        }
    }

    private fun saveDownloadedDataToLocal() {
        stickerPackModal ?: return
        downloadedFiles = 0
        totalFilesToDownload = 0

        allDownloadedStickerPacks.add(stickerPackModal!!)

        userPref.resetPrefs(this)
        userPref.saveStickerPacks(allDownloadedStickerPacks)
    }
}