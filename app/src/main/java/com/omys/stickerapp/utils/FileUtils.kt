package com.omys.stickerapp.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

fun Context.getFileExtensionFromUri(uri: Uri): String? {
    val extension: String
    val mimeTypeMap = MimeTypeMap.getSingleton()
    extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)).toString()
    return ".$extension"
}

fun File.nameWithExtension(): String {
    return "$nameWithoutExtension.$extension"
}

fun Context.getTrayImagesDirectory(identifier: String): String {
    val folder = "$filesDir/$APP_DIRECTORY/$identifier/$TRAY_FILE_PATH"
    val file = File(folder)
    if (!file.exists()) {
        file.mkdirs()
    }
    return folder
}

fun Context.getStickerFilesDirectory(identifier: String): String {
    val folder = "$filesDir/$APP_DIRECTORY/$identifier/"
    val file = File(folder)
    if (!file.exists()) {
        file.mkdirs()
    }
    return folder
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