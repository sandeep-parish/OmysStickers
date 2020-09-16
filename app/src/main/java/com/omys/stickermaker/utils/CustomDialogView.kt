package com.omys.stickermaker.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ProgressBar

class CustomDialogView(context: Context?) : Dialog(context!!) {
    private val progressBar: ProgressBar = ProgressBar(context)

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        setContentView(progressBar)
        setCanceledOnTouchOutside(false)
    }
}