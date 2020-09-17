package com.omys.stickermaker.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.omys.stickermaker.R
import com.omys.stickermaker.adapter.StickersListAdapter
import com.omys.stickermaker.helpers.FirebaseHelper
import com.omys.stickermaker.helpers.OnStickerPackUpdate
import com.omys.stickermaker.helpers.OnUploadCallback
import com.omys.stickermaker.modal.StickerPackInfoModal
import com.omys.stickermaker.utils.*
import kotlinx.android.synthetic.main.activity_sticker_pack_details.*

fun Context.startStickerPackDetailsActivity(stickerPackInfoModal: StickerPackInfoModal?) {
    val intent = Intent(this, StickerPackDetailsActivity::class.java)
    intent.putExtra(ARGS1, stickerPackInfoModal)
    startActivity(intent)
}

class StickerPackDetailsActivity : AppCompatActivity(), OnUploadCallback, OnStickerPackUpdate {

    private var index = 0
    private var stickersUrls = ArrayList<String>()

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

        bindUIViews()
    }

    private fun bindUIViews() {
        firebaseHelper = FirebaseHelper(this)

        stickersList?.adapter = stickersListAdapter
        stickerTrayImage?.loadImage(stickerPackInfoModal?.tray_image_file.toString())
        stickerPackName?.text = stickerPackInfoModal?.name.toString()
        stickerAuthor?.text = stickerPackInfoModal?.publisher.toString()
        toolbar?.title = stickerPackInfoModal?.name.toString()

        if (!stickerPackInfoModal?.stickers.isNullOrEmpty()) {
            stickersListAdapter.setStickerPacks(stickerPackInfoModal?.stickers)
        }
        setOnClickListener()
    }

    private fun setOnClickListener() {
        toolbar?.setNavigationOnClickListener { super.onBackPressed() }

        addNewStickersToPack?.setOnClickListener {
            if (stickersListAdapter.itemCount >= 30) {
                showToast(getString(R.string.stickerMaxLimit))
            } else {
                openFilePicker(isMultiple = true)
            }
        }

        publishStickerPack?.setOnClickListener {
            if (stickersListAdapter.itemCount < 3) {
                showToast(getString(R.string.minStickerLimit))
            } else {
                val firstStickerUri = stickersListAdapter.stickers[0]
                firebaseHelper?.uploadFile(Uri.parse(firstStickerUri), index.toString(), this)
            }
        }

        add_to_whatsapp_button?.setOnClickListener {
            if (stickerPackInfoModal?.stickers?.size!! >= 3) {
                addStickerPackToWhatsApp(stickerPackInfoModal)
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
        }
    }

    override fun onUploadComplete(identifier: String, fileUrl: String) {
        stickersUrls.add(fileUrl)

        if (stickersUrls.size < stickersListAdapter.itemCount) {
            index++
            firebaseHelper?.uploadFile(Uri.parse(stickersListAdapter.stickers[index]), index.toString(), this)
        } else {
            firebaseHelper?.updateStickerPackData(stickerPackInfoModal?.id, mapOf(
                    KEY_STICKERS to stickersUrls
            ), this)
        }
    }

    override fun onPackUpdated(isSuccessful: Boolean) {
        showToast("${stickersListAdapter.itemCount} Stickers successfully added to ${stickerPackInfoModal?.name}")
    }

}