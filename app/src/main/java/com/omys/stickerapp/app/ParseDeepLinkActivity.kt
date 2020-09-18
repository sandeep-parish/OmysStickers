package com.omys.stickerapp.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.omys.stickerapp.R
import com.omys.stickerapp.database.OmysDatabase
import com.omys.stickerapp.database.StickerPacksDao
import com.omys.stickerapp.helpers.FirebaseHelper
import com.omys.stickerapp.helpers.OnStickerPackInfo
import com.omys.stickerapp.modal.StickerPackInfoModal
import com.omys.stickerapp.utils.*

class ParseDeepLinkActivity : AppCompatActivity(), OnStickerPackInfo {

    private var SPLASH_TIMEOUT = 3000L
    private var database: StickerPacksDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        database = OmysDatabase.getDatabase(this).stickerPacksDatabase()
        Handler(Looper.getMainLooper()).postDelayed({
            if (intent.data != null && !intent.data?.getQueryParameter(TYPE_TAG).isNullOrEmpty()) {
                handleDeepLinkIntents()
            } else {
                startAllStickersActivity(true)
                finish()
            }
        }, SPLASH_TIMEOUT)

    }

    private fun handleDeepLinkIntents() {
        val typeTag = intent.data?.getQueryParameter(TYPE_TAG).toString()
        val identifier = intent.data?.getQueryParameter(KEY_ID).toString()
        debugPrint("$typeTag and id $identifier")

        if (typeTag.equals(TYPE_STICKER_PACK, true) && identifier.isNotEmpty()) {
            val offlineStickerPack = database?.getOfflineStickerPackById(identifier)
            if (offlineStickerPack != null) {
                onStickerPackDetails(offlineStickerPack)
            } else {
                FirebaseHelper(this).getStickerPackDetails(identifier, false, this)
            }
        } else {
            startAllStickersActivity(true)
            finish()
        }
    }

    override fun onStickerPackDetails(stickerPack: StickerPackInfoModal?) {
        if (stickerPack != null) {
            startStickerPackDetailsActivity(stickerPack)
            finish()
        } else {
            showToast(getString(R.string.stickerLinkNotValid))
            startAllStickersActivity(true)
            finish()
        }
    }


}