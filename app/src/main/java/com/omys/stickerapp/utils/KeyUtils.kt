package com.omys.stickerapp.utils

import com.omys.stickerapp.BuildConfig

const val ARGS1 = "args1"

const val DIR_TRAY_IMG = "TrayImages/"
const val DIR_STICKERS = "Stickers/"


//Firestore Tables
const val STICKER_PACKS_COLLECTION = "StickerPacks"

const val KEY_CREATED_AT = "createdAt"
const val KEY_STICKERS = "stickers"
const val KEY_TOTAL_STICKERS = "totalStickers"

//file types
const val FILE_IMAGE = "image/*"
const val TRAY_IMAGE = "tray_image"
const val STICKER = "sticker"

//Request Codes
const val FILE_PICKER_REQUEST_CODE = 1001
const val ADD_STICKER_PACK_CODE = 1002

//Static data
const val PUBLISHER_EMAIL = "sk.parish01@gmail.com"
const val PRIVACY_POLICY = ""
const val PUBLISHER_WEBSITE = ""
const val IMAGE_DATA_VERSION = "1"
const val ANDROID_PLAY_STORE_LINK = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

const val APP_DIRECTORY = "Omys Stickers/"
const val TRAY_FILE_PATH = "TrayImages/"

const val TYPE_TAG = "type_tag"
const val TYPE_STICKER_PACK = "StickerPack"
const val KEY_ID = "id"

const val REFRESH_INTERVAL = 1 * 1000 * 60 * 60

//Whatsapp Constants
const
val ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"

//For admin feature
val IS_ADMIN_RIGHTS = BuildConfig.DEBUG