package com.omys.stickermaker.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.omys.stickermaker.BuildConfig
import com.omys.stickermaker.R

fun debugPrint(message: String?) {
    if (BuildConfig.DEBUG) {
        Log.e("Print", message.toString())
    }
}

fun Context.showToast(message: String?) {
    message ?: return
    Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show()
}

fun ImageView.loadImage(imageUrl: String, placeholder: Int = R.drawable.ic_image_place_holder) {
    Glide.with(context)
            .load(imageUrl)
            .apply(RequestOptions()
                    .placeholder(placeholder)
                    .error(placeholder))
            .into(this)
}


fun View.avoidDoubleClicks() {
    val DELAY_IN_MS: Long = 900
    if (!this.isClickable) {
        return
    }
    this.isClickable = false
    this.postDelayed({ this.isClickable = true }, DELAY_IN_MS)
}

fun View.hide() {
    animate().alpha(0.0f)?.duration = 500
    visibility = View.GONE
}

fun View.visible(opacity: Float = 1.0f) {
    animate().alpha(opacity).duration = 500
    visibility = View.VISIBLE
}

fun Activity.openFilePicker(fileType: String = FILE_IMAGE, isMultiple: Boolean = false, requestCode: Int = FILE_PICKER_REQUEST_CODE) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.type = fileType
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultiple);
    startActivityForResult(intent, requestCode)
}

fun AppCompatActivity.changeStatusBarColor(@ColorRes color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window: Window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, color)
    }
}


fun Activity.addStickerPackToWhatsApp(identifier: String?, packName: String?) {
    if (identifier.isNullOrEmpty() || packName.isNullOrEmpty()) return
    val intent = Intent()
    intent.action = ACTION_ENABLE_STICKER_PACK
    intent.putExtra(EXTRA_STICKER_PACK_ID, identifier)
    intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
    intent.putExtra(EXTRA_STICKER_PACK_NAME, packName)
    try {
        startActivityForResult(intent, ADD_STICKER_PACK_CODE)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show()
    }
}
