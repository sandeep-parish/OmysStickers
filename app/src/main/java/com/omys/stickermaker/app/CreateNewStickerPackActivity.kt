package com.omys.stickermaker.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.omys.stickermaker.R
import com.omys.stickermaker.helpers.FirebaseHelper
import com.omys.stickermaker.helpers.OnSickerPackCallback
import com.omys.stickermaker.helpers.OnUploadCallback
import com.omys.stickermaker.modal.StickerPackInfoModal
import com.omys.stickermaker.utils.*
import kotlinx.android.synthetic.main.activity_create_new_sticker_pack.*

fun Context.startCreateNewSticker() {
    val intent = Intent(this, CreateNewStickerPackActivity::class.java)
    startActivity(intent)
}

class CreateNewStickerPackActivity : AppCompatActivity(), OnUploadCallback, OnSickerPackCallback {
    private var fileUri: Uri? = null
    private var firebaseHelper: FirebaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_sticker_pack)

        bindUiViews()
    }

    private fun bindUiViews() {
        firebaseHelper = FirebaseHelper(this)

        stickerTrayImage?.setOnClickListener {
            openFilePicker()
        }

        btnSubmitPack?.setOnClickListener {
            if (etPackName.text.toString().trim().isNotEmpty() && etPackCreator.text.toString().trim().isNotEmpty() && fileUri.toString().trim().isNotEmpty()) {
                firebaseHelper?.uploadFile(fileUri, TRAY_IMAGE, this, DIR_TRAY_IMG)
            } else {
                Toast.makeText(this, "Enter filed data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FILE_PICKER_REQUEST_CODE -> {
                val uri = data?.data
                uri?.let { contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                val fileExtension = getFileExtensionFromUri(uri!!)
                if (fileExtension.equals(".png", true)) {
                    fileUri = uri
                    stickerTrayImage?.setImageURI(fileUri)
                } else {
                    showToast("$fileExtension is not supported for tray images")
                }

            }
        }
    }

    override fun onUploadComplete(identifier: String, fileUrl: String) {
        when (identifier) {
            TRAY_IMAGE -> {
                createNewStickerPack(fileUrl)
            }
        }
    }

    private fun createNewStickerPack(fileUrl: String) {
        val stickerPack = StickerPackInfoModal()
        stickerPack.name = etPackName.text.toString().capitalize()
        stickerPack.publisher = etPackCreator.text.toString().trim().capitalize()
        stickerPack.tray_image_file = fileUrl
        stickerPack.createdAt = System.currentTimeMillis()
        stickerPack.totalStickers = 0
        firebaseHelper?.createNewStickerPack(stickerPack, this)
    }

    override fun onPackCreated(stickerPackInfoModal: StickerPackInfoModal?) {
        if (stickerPackInfoModal != null) {
            startStickerPackDetailsActivity(stickerPackInfoModal)
        } else {
            showToast("Something went wrong")
        }
    }
}