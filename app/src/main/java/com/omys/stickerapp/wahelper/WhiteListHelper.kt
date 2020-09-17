/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.omys.stickerapp.wahelper

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.omys.stickerapp.BuildConfig

object WhiteListHelper {
    private const val AUTHORITY_QUERY_PARAM = "authority"
    private const val IDENTIFIER_QUERY_PARAM = "identifier"
    private const val STICKER_APP_AUTHORITY = BuildConfig.CONTENT_PROVIDER_AUTHORITY
    private const val CONSUMER_WHATSAPP_PACKAGE_NAME = "com.whatsapp"
    private const val SMB_WHATSAPP_PACKAGE_NAME = "com.whatsapp.w4b"
    private const val CONTENT_PROVIDER = ".provider.sticker_whitelist_check"
    private const val QUERY_PATH = "is_whitelisted"
    private const val QUERY_RESULT_COLUMN_NAME = "result"

    fun isWhitelisted(context: Context, identifier: String): Boolean {
        return try {
            val consumerResult = isWhitelistedFromProvider(context, identifier, CONSUMER_WHATSAPP_PACKAGE_NAME)
            val smbResult = isWhitelistedFromProvider(context, identifier, SMB_WHATSAPP_PACKAGE_NAME)
            consumerResult && smbResult
        } catch (e: Exception) {
            false
        }
    }

    private fun isWhitelistedFromProvider(context: Context, identifier: String, whatsappPackageName: String): Boolean {
        val packageManager = context.packageManager
        if (isPackageInstalled(whatsappPackageName, packageManager)) {
            val whatsappProviderAuthority = whatsappPackageName + CONTENT_PROVIDER
            packageManager.resolveContentProvider(whatsappProviderAuthority, PackageManager.GET_META_DATA)
                    ?: return false
            val queryUri = Uri.Builder().scheme(StickerContentProvider.CONTENT_SCHEME).authority(whatsappProviderAuthority).appendPath(QUERY_PATH).appendQueryParameter(AUTHORITY_QUERY_PARAM, STICKER_APP_AUTHORITY).appendQueryParameter(IDENTIFIER_QUERY_PARAM, identifier).build()
            context.contentResolver.query(queryUri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val whiteListResult = cursor.getInt(cursor.getColumnIndexOrThrow(QUERY_RESULT_COLUMN_NAME))
                    return whiteListResult == 1
                }
            }
        } else {
            //if app is not installed, then don't need to take into its whitelist info into account.
            return true
        }
        return false
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            applicationInfo.enabled ?: false
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}