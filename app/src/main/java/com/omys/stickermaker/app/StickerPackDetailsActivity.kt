package com.omys.stickermaker.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.omys.stickermaker.R
import com.omys.stickermaker.adapter.StickersListAdapter
import com.omys.stickermaker.helpers.FirebaseHelper
import com.omys.stickermaker.helpers.OnStickerPackUpdate
import com.omys.stickermaker.helpers.OnUploadCallback
import com.omys.stickermaker.modal.StickerPack
import com.omys.stickermaker.utils.*
import kotlinx.android.synthetic.main.activity_sticker_pack_details.*

fun Activity.startStickerPackDetailsActivity(stickerPack: StickerPack?) {
    val intent = Intent(this, StickerPackDetailsActivity::class.java)
    intent.putExtra(ARGS1, stickerPack)
    startActivity(intent)
}

class StickerPackDetailsActivity : AppCompatActivity(), OnUploadCallback, OnStickerPackUpdate {

    private var index = 0
    private var stickersUrls = ArrayList<String>()

    private var firebaseHelper: FirebaseHelper? = null
    private val stickersListAdapter by lazy { StickersListAdapter() }
    private var stickerPack: StickerPack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker_pack_details)

        if (intent.extras?.containsKey(ARGS1) == true) {
            stickerPack = intent?.extras?.getParcelable(ARGS1)
        }
        if (stickerPack == null) {
            showToast("Sticker pack is not valid")
            super.onBackPressed()
        }

        bindUIViews()
    }

    private fun bindUIViews() {
        firebaseHelper = FirebaseHelper(this)

        stickersList?.adapter = stickersListAdapter
        stickerTrayImage?.loadImage(stickerPack?.tray_image_file.toString())
        stickerPackName?.text = stickerPack?.name.toString()
        stickerAuthor?.text = stickerPack?.publisher.toString()
        toolbar?.title = stickerPack?.name.toString()

        if (!stickerPack?.stickers.isNullOrEmpty()) {
            stickersListAdapter.setStickerPacks(stickerPack?.stickers)
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
            if (stickerPack?.stickers?.size!! >= 3) {
                addStickerPackToWhatsApp(stickerPack)
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
            firebaseHelper?.updateStickerPackData(stickerPack?.identifier, mapOf(
                    KEY_STICKERS to stickersUrls
            ), this)
        }
    }

    override fun onPackUpdated(isSuccessful: Boolean) {
        showToast("${stickersListAdapter.itemCount} Stickers successfully added to ${stickerPack?.name}")
    }

}