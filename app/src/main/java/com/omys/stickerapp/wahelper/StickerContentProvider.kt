package com.omys.stickerapp.wahelper

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import com.omys.stickerapp.BuildConfig
import com.omys.stickerapp.database.OmysDatabase
import com.omys.stickerapp.database.StickerPacksDao
import com.omys.stickerapp.modal.StickerPackModal
import com.omys.stickerapp.utils.APP_DIRECTORY
import com.omys.stickerapp.utils.debugPrint
import com.omys.stickerapp.utils.getStickerFilesDirectory
import com.omys.stickerapp.utils.getTrayImagesDirectory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class StickerContentProvider : ContentProvider() {

    private var stickersPack: StickerPacksDao? = null

    override fun onCreate(): Boolean {
        val authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY
        check(authority.startsWith(context?.packageName.toString())) {
            "your authority (" + authority + ") for the content provider should start with your package name: " + context?.packageName
        }
        stickersPack = OmysDatabase.getDatabase(context).stickerPacksDatabase()

        //the call to get the metadata for the sticker packs.
        MATCHER.addURI(authority, METADATA, METADATA_CODE)

        //the call to get the metadata for single sticker pack. * represent the identifier
        MATCHER.addURI(authority, "$METADATA/*", METADATA_CODE_FOR_SINGLE_PACK)

        //gets the list of stickers for a sticker pack, * respresent the identifier.
        MATCHER.addURI(authority, "$STICKERS/*", STICKERS_CODE)
        for (stickerPack in getStickerPacksList()) {
            MATCHER.addURI(authority, APP_DIRECTORY + "/" + stickerPack.identifier + "/" + stickerPack.trayImageFile, STICKER_PACK_TRAY_ICON_CODE)
            for (sticker in stickerPack.stickers) {
                MATCHER.addURI(authority, APP_DIRECTORY + "/" + stickerPack.identifier + "/" + sticker.imageFileName, STICKERS_ASSET_CODE)
            }
        }
        return true
    }

    /**always Query sticker packs with updated data*/
    private fun getStickerPacksList(): ArrayList<StickerPackModal> {
        val stickerPacks = ArrayList<StickerPackModal>()
        stickerPacks.addAll(stickersPack?.getAllStickerPacks()!!)
        return stickerPacks
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val code = MATCHER.match(uri)
        return if (code == METADATA_CODE) {
            getPackForAllStickerPacks(uri)
        } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
            getCursorForSingleStickerPack(uri)
        } else if (code == STICKERS_CODE) {
            getStickersForAStickerPack(uri)
        } else {
            throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    @Throws(FileNotFoundException::class)
    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        MATCHER.match(uri)
        return getImageAsset(uri)
    }

    @Throws(IllegalArgumentException::class)
    private fun getImageAsset(uri: Uri): AssetFileDescriptor? {
        val pathSegments = uri.pathSegments
        require(pathSegments.size == 3) { "path segments should be 3, uri is: $uri" }
        val fileName = pathSegments[pathSegments.size - 1]
        val identifier = pathSegments[pathSegments.size - 2]
        require(!TextUtils.isEmpty(identifier)) { "identifier is empty, uri: $uri" }
        require(!TextUtils.isEmpty(fileName)) { "file name is empty, uri: $uri" }
        //making sure the file that is trying to be fetched is in the list of stickers.
        for (stickerPack in getStickerPacksList()) {
            if (identifier == stickerPack.identifier) {
                if (fileName == stickerPack.trayImageFile) {
                    return fetchFile(uri, context?.assets!!, fileName, identifier)
                } else {
                    for (sticker in stickerPack.stickers) {
                        if (fileName == sticker.imageFileName) {
                            return fetchFile(uri, context?.assets!!, fileName, identifier)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun fetchFile(uri: Uri, am: AssetManager, fileName: String, identifier: String): AssetFileDescriptor? {
        return try {
            val file = if (fileName.endsWith(".png")) {
                File(context!!.getTrayImagesDirectory(identifier), fileName)
            } else {
                File(context!!.getStickerFilesDirectory(identifier) + "/", fileName)
            }
            if (!file.exists()) {
                debugPrint("StickerPackModal dir not found")
            }
            AssetFileDescriptor(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), 0L, -1L)
        } catch (e: IOException) {
            Log.e(context?.packageName, "IOException when getting asset file, uri:$uri", e)
            null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (MATCHER.match(uri)) {
            METADATA_CODE -> "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA
            METADATA_CODE_FOR_SINGLE_PACK -> "vnd.android.cursor.item/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + METADATA
            STICKERS_CODE -> "vnd.android.cursor.dir/vnd." + BuildConfig.CONTENT_PROVIDER_AUTHORITY + "." + STICKERS
            STICKERS_ASSET_CODE -> "image/webp"
            STICKER_PACK_TRAY_ICON_CODE -> "image/png"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    private fun getPackForAllStickerPacks(uri: Uri): Cursor {
        return getStickerPackInfo(uri, getStickerPacksList())
    }

    private fun getCursorForSingleStickerPack(uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        for (stickerPack in getStickerPacksList()) {
            if (identifier == stickerPack.identifier) {
                return getStickerPackInfo(uri, listOf(stickerPack))
            }
        }
        return getStickerPackInfo(uri, ArrayList())
    }

    private fun getStickerPackInfo(uri: Uri, stickerPackList: List<StickerPackModal>): Cursor {
        val cursor = MatrixCursor(arrayOf(
                STICKER_PACK_IDENTIFIER_IN_QUERY,
                STICKER_PACK_NAME_IN_QUERY,
                STICKER_PACK_PUBLISHER_IN_QUERY,
                STICKER_PACK_ICON_IN_QUERY,
                ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                PUBLISHER_EMAIL,
                PUBLISHER_WEBSITE,
                PRIVACY_POLICY_WEBSITE,
                LICENSE_AGREENMENT_WEBSITE
        ))
        for (stickerPack in stickerPackList) {
            val builder = cursor.newRow()
            builder.add(stickerPack.identifier)
            builder.add(stickerPack.name)
            builder.add(stickerPack.publisher)
            builder.add(stickerPack.trayImageFile)
            builder.add(stickerPack.androidPlayStoreLink)
            builder.add(stickerPack.iosAppStoreLink)
            builder.add(stickerPack.publisherEmail)
            builder.add(stickerPack.publisherWebsite)
            builder.add(stickerPack.privacyPolicyWebsite)
            builder.add(stickerPack.licenseAgreementWebsite)
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    private fun getStickersForAStickerPack(uri: Uri): Cursor {
        val identifier = uri.lastPathSegment
        val cursor = MatrixCursor(arrayOf(STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY))
        for (stickerPack in getStickerPacksList()) {
            if (identifier == stickerPack.identifier) {
                for (sticker in stickerPack.stickers) {
                    cursor.addRow(arrayOf<Any?>(sticker.imageFileName, TextUtils.join(",", sticker.emojis)))
                }
            }
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not supported")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Not supported")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Not supported")
    }

    /**
     * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between sticker app and WhatsApp.
     */
    companion object {
        const val STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier"
        const val STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name"
        const val STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher"
        const val STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon"
        const val ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link"
        const val IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link"
        const val PUBLISHER_EMAIL = "sticker_pack_publisher_email"
        const val PUBLISHER_WEBSITE = "sticker_pack_publisher_website"
        const val PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website"
        const val LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website"
        const val STICKER_FILE_NAME_IN_QUERY = "sticker_file_name"
        const val STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji"
        const val CONTENT_SCHEME = "content"


        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH)
        const val METADATA = "metadata"
        private const val METADATA_CODE = 1
        private const val METADATA_CODE_FOR_SINGLE_PACK = 2
        const val STICKERS = "stickers"
        private const val STICKERS_CODE = 3
        private const val STICKERS_ASSET_CODE = 4
        private const val STICKER_PACK_TRAY_ICON_CODE = 5
    }
}