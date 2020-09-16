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
import com.omys.stickermaker.modal.StickerPack
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
                fileUri = data?.data
                fileUri?.let { contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                stickerTrayImage?.setImageURI(fileUri)
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
        val stickerPack = StickerPack()
        stickerPack.name = etPackName.text.toString()
        stickerPack.publisher = etPackCreator.text.toString().trim()
        stickerPack.tray_image_file = fileUrl
        stickerPack.publisher_email = PUBLISHER_EMAIL
        stickerPack.publisher_website = PUBLISHER_WEBSITE
        stickerPack.privacy_policy_website = PRIVACY_POLICY
        stickerPack.license_agreement_website = PUBLISHER_EMAIL
        stickerPack.image_data_version = IMAGE_DATA_VERSION
        stickerPack.createdAt = System.currentTimeMillis()
        firebaseHelper?.createNewStickerPack(stickerPack, this)
    }

    override fun onPackCreated(stickerPack: StickerPack?) {
        if (stickerPack != null) {
            startStickerPackDetailsActivity(stickerPack)
        } else {
            showToast("Something went wrong")
        }
    }
}