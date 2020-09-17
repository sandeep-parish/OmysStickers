package com.omys.stickerapp

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

class OmysApp : MultiDexApplication() {
    companion object {
        var appContext: OmysApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this

        MultiDex.install(this)
        Firebase.initialize(this)
    }
}