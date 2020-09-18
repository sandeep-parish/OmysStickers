package com.omys.stickerapp.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.omys.stickerapp.R

class SplashActivity : AppCompatActivity() {

    private var SPLASH_TIMEOUT = 3000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startAllStickersActivity(true)
            finish()
        }, SPLASH_TIMEOUT)

    }
}