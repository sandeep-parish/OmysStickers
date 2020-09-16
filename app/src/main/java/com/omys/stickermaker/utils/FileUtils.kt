package com.omys.stickermaker.utils

import android.graphics.*
import android.media.ExifInterface
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


fun getCompressedImageFile(imagePath: String?, quality: Int = 80): String? {
    val maxHeight = 1280.0f
    val maxWidth = 1280.0f
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    var scaledBitmap: Bitmap? = null

    var bmp = BitmapFactory.decodeFile(imagePath, options)
    var actualHeight = options.outHeight
    var actualWidth = options.outWidth
    var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
    val maxRatio = maxWidth / maxHeight
    if (actualHeight > maxHeight || actualWidth > maxWidth) {
        if (imgRatio < maxRatio) {
            imgRatio = maxHeight / actualHeight
            actualWidth = (imgRatio * actualWidth).toInt()
            actualHeight = maxHeight.toInt()
        } else if (imgRatio > maxRatio) {
            imgRatio = maxWidth / actualWidth
            actualHeight = (imgRatio * actualHeight).toInt()
            actualWidth = maxWidth.toInt()
        } else {
            actualHeight = maxHeight.toInt()
            actualWidth = maxWidth.toInt()
        }
    }
    options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
    options.inJustDecodeBounds = false
    options.inDither = false
    options.inPurgeable = true
    options.inInputShareable = true
    options.inTempStorage = ByteArray(16 * 1024)
    try {
        bmp = BitmapFactory.decodeFile(imagePath, options)
    } catch (exception: OutOfMemoryError) {
        exception.printStackTrace()
    }
    try {
        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565)
    } catch (exception: OutOfMemoryError) {
        exception.printStackTrace()
    }
    val ratioX = actualWidth / options.outWidth.toFloat()
    val ratioY = actualHeight / options.outHeight.toFloat()
    val middleX = actualWidth / 2.0f
    val middleY = actualHeight / 2.0f
    val scaleMatrix = Matrix()
    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
    val canvas = Canvas(scaledBitmap!!)
    canvas.setMatrix(scaleMatrix)
    canvas.drawBitmap(bmp!!, middleX - bmp.width / 2, middleY - bmp.height / 2, Paint(Paint.FILTER_BITMAP_FLAG))
    bmp.recycle()

    try {
        val exif = ExifInterface(imagePath.toString())
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width,
                scaledBitmap.height, getRotationMatrix(exif), true)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    try {
        val out = FileOutputStream(imagePath)
        scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, quality, out)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    return imagePath
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
        val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
        inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
    }
    val totalPixels = width * height.toFloat()
    val totalReqPixelsCap = reqWidth * reqHeight * 2.toFloat()
    while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
        inSampleSize++
    }
    return inSampleSize
}

private fun getRotationMatrix(exitInterface: ExifInterface): Matrix? {
    val orientation = exitInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> {
            matrix.postRotate(90f)
        }
        ExifInterface.ORIENTATION_ROTATE_180 -> {
            matrix.postRotate(180f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> {
            matrix.postRotate(270f)
        }
    }
    return matrix
}


fun String?.deleteOriginalFile() {
    if (this.isNullOrEmpty()) return

    val file = File(this)
    if (file.exists()) {
        file.delete()
        debugPrint("File ${file.absolutePath} is deleted")
    } else {
        debugPrint("$this File not exists")
    }
}