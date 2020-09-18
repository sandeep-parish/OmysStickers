package com.omys.stickerapp.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.omys.stickerapp.R
import com.omys.stickerapp.adapter.StickersListAdapter
import com.omys.stickerapp.database.OmysDatabase
import com.omys.stickerapp.database.StickerPacksDao
import com.omys.stickerapp.helpers.*
import com.omys.stickerapp.modal.StickerPackInfoModal
import com.omys.stickerapp.modal.StickerPackModal
import com.omys.stickerapp.utils.*
import com.omys.stickerapp.wahelper.WhiteListHelper
import kotlinx.android.synthetic.main.activity_sticker_pack_details.*
import kotlinx.android.synthetic.main.app_toolbar.view.*

fun Context.startStickerPackDetailsActivity(stickerPackInfoModal: StickerPackInfoModal?) {
    val intent = Intent(this, StickerPackDetailsActivity::class.java)
    intent.putExtra(ARGS1, stickerPackInfoModal)
    startActivity(intent)
}

class StickerPackDetailsActivity : AppCompatActivity(), OnUploadCallback, OnStickerPackUpdate, OnStickerPackCallBack {

    private var index = 0
    private var stickersUrls = ArrayList<String>()
    private var stickerPacks: StickerPacksDao? = null
    private val stickerPackHelper by lazy { StickerPackHelper(this) }

    private var isAddedToWhatsapp = false
    private var firebaseHelper: FirebaseHelper? = null
    private val stickersListAdapter by lazy { StickersListAdapter() }
    private var stickerPackInfoModal: StickerPackInfoModal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker_pack_details)

        if (intent.extras?.containsKey(ARGS1) == true) {
            stickerPackInfoModal = intent?.extras?.getParcelable(ARGS1)
        }
        if (stickerPackInfoModal == null) {
            showToast("Sticker pack is not valid")
            super.onBackPressed()
        }
        isAddedToWhatsapp = WhiteListHelper.isWhitelisted(this, stickerPackInfoModal?.id.toString())

        bindUIViews()
    }

    private fun bindUIViews() {
        firebaseHelper = FirebaseHelper(this)
        stickerPacks = OmysDatabase.getDatabase(this).stickerPacksDatabase()

        if (stickerPacks?.getOfflineStickerPackById(stickerPackInfoModal?.id) == null) {
            stickerPacks?.addNewStickerPackLocally(stickerPackInfoModal)
        }

        stickersList?.adapter = stickersListAdapter
        stickerTrayImage?.loadImage(stickerPackInfoModal?.tray_image_file.toString())
        stickerPackName?.text = stickerPackInfoModal?.name.toString()
        stickerAuthor?.text = stickerPackInfoModal?.publisher.toString()
        includeToolbar?.title?.text = stickerPackInfoModal?.name.toString()

        if (!stickerPackInfoModal?.stickers.isNullOrEmpty()) {
            stickersListAdapter.setStickerPacks(stickerPackInfoModal?.stickers)
        }

        if (isAddedToWhatsapp) {
            viewAlreadyAdded.visible()
            btnAddToWhatsApp.hide()
        }

        setOnClickListener()
    }

    private fun setOnClickListener() {
        includeToolbar?.btnBack?.setOnClickListener { super.onBackPressed() }

        addNewStickersToPack?.setOnClickListener {
            if (stickersListAdapter.itemCount >= 30) {
                showToast(getString(R.string.stickerMaxLimit))
            } else {
                openFilePicker(isMultiple = true)
            }
        }

        if (IS_ADMIN_RIGHTS) {
            addNewStickersToPack.visible()
            includeToolbar?.rightButton?.text = getString(R.string.save)
            includeToolbar?.rightButton?.setOnClickListener {
                if (stickersListAdapter.itemCount < 3) {
                    showToast(getString(R.string.minStickerLimit))
                } else {
                    val firstStickerUri = stickersListAdapter.stickers[0]
                    firebaseHelper?.uploadFile(Uri.parse(firstStickerUri), index.toString(), this)
                }
            }
        }

        btnAddToWhatsApp?.setOnClickListener {
            when {
                stickersListAdapter.itemCount < 3 -> {
                    showToast(getString(R.string.minStickerLimit))
                }
                stickerPacks?.getStickerPackById(stickerPackInfoModal?.id) != null -> {
                    addStickerPackToWhatsApp(stickerPackInfoModal?.id, stickerPackInfoModal?.name)
                }
                else -> {
                    stickerPackHelper.startDownloadingStickerPack(stickerPackInfoModal!!, this);
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FILE_PICKER_REQUEST_CODE -> {
                if (data != null) {
                    val clipData = data.clipData

                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val path = clipData.getItemAt(i).uri
                            contentResolver.takePersistableUriPermission(path, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            stickersListAdapter.addNewSticker(path.toString())
                        }
                    } else {
                        val uri = data.data
                        uri?.let { contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        stickersListAdapter.addNewSticker(uri.toString())
                    }
                }
            }
            ADD_STICKER_PACK_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    showToast("Sticker pack added successfully")
                    viewAlreadyAdded.visible()
                    btnAddToWhatsApp.hide()
                } else {
                    val validationError = data?.getStringExtra("validation_error")
                    debugPrint(validationError.toString())
                }
            }
        }
    }

    override fun onUploadComplete(identifier: String, fileUrl: String) {
        stickersUrls.add(fileUrl)

        if (stickersUrls.size < stickersListAdapter.itemCount) {
            index++
            firebaseHelper?.uploadFile(Uri.parse(stickersListAdapter.stickers[index]), index.toString(), this)
        } else {
            firebaseHelper?.updateStickerPackData(stickerPackInfoModal?.id, mapOf(
                    KEY_STICKERS to stickersUrls,
                    KEY_TOTAL_STICKERS to stickersUrls.size,
                    KEY_CREATED_AT to System.currentTimeMillis()
            ), this)
        }
    }

    override fun onPackUpdated(isSuccessful: Boolean) {
        showToast("${stickersListAdapter.itemCount} Stickers successfully added to ${stickerPackInfoModal?.name}")
    }

    override fun onStickerPackDownloaded(stickerPack: StickerPackModal?) {
        stickerPacks?.addNewStickerPack(stickerPack)

        addStickerPackToWhatsApp(stickerPack?.identifier, stickerPack?.name)
    }
}