package com.omys.stickerapp.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.omys.stickerapp.R
import com.omys.stickerapp.helpers.FirebaseHelper
import com.omys.stickerapp.helpers.OnStickerPackInfo
import com.omys.stickerapp.modal.StickerPackInfoModal
import com.omys.stickerapp.utils.KEY_ID
import com.omys.stickerapp.utils.TYPE_STICKER_PACK
import com.omys.stickerapp.utils.TYPE_TAG
import com.omys.stickerapp.utils.showToast

class ParseDeepLinkActivity : AppCompatActivity(), OnStickerPackInfo {

    private var SPLASH_TIMEOUT = 3000L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (intent.extras != null && !intent.data?.getQueryParameter(TYPE_TAG).isNullOrEmpty()) {
                handleDeepLinkIntents()
            } else {
                startAllStickersActivity(true)
                finish()
            }
        }, SPLASH_TIMEOUT)

    }

    private fun handleDeepLinkIntents() {
        val typeTag = intent.data?.getQueryParameter(TYPE_TAG)
        val identifier = intent.data?.getQueryParameter(KEY_ID)

        if (typeTag?.equals(TYPE_STICKER_PACK, true) == true
                && !identifier.isNullOrEmpty()) {
            FirebaseHelper(this).getStickerPackDetails(identifier, false, this)
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