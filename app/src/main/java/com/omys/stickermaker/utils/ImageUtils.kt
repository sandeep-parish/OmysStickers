package com.omys.stickermaker.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

fun String.optimizeAsSticker(quality: Int = 80): File {
    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    bmOptions.outWidth = 512
    bmOptions.outHeight = 512
    val bitmap = BitmapFactory.decodeFile(this, bmOptions)
    val out = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, false)

    val file = File(this)
    val fOut: FileOutputStream
    try {
        fOut = FileOutputStream(file)
        if (file.extension.equals("webp", true)) {
            out.compress(Bitmap.CompressFormat.WEBP, quality, fOut)
        } else {
            out.compress(Bitmap.CompressFormat.PNG, quality, fOut)
        }
        fOut.flush()
        fOut.close()
        bitmap.recycle()
        out.recycle()
    } catch (e: Exception) {
    }
    return file
}

fun resizeImage(file: File, scaleTo: Int = 1024) {
    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(file.absolutePath, bmOptions)
    val photoW = bmOptions.outWidth
    val photoH = bmOptions.outHeight

    // Determine how much to scale down the image
    val scaleFactor = Math.min(photoW / scaleTo, photoH / scaleTo)

    bmOptions.inJustDecodeBounds = false
    bmOptions.inSampleSize = scaleFactor

    val resized = BitmapFactory.decodeFile(file.absolutePath, bmOptions) ?: return
    file.outputStream().use {
        resized.compress(Bitmap.CompressFormat.JPEG, 75, it)
        resized.recycle()
    }
}