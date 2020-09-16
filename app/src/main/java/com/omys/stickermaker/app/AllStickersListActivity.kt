package com.omys.stickermaker.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.omys.stickermaker.R
import com.omys.stickermaker.adapter.StickerPacksListAdapter
import com.omys.stickermaker.modal.StickerPack
import com.omys.stickermaker.utils.CustomDialogView
import com.omys.stickermaker.utils.KEY_CREATED_AT
import com.omys.stickermaker.utils.KEY_STICKER_PACK
import com.omys.stickermaker.utils.avoidDoubleClicks
import kotlinx.android.synthetic.main.activity_all_stickers_list.*

class AllStickersListActivity : AppCompatActivity(), StickerPacksListAdapter.OnStickerPackAction {

    private var dialogView: CustomDialogView? = null
    private val stickerPacksAdapter by lazy { StickerPacksListAdapter(this) }
    private var firestore: FirebaseFirestore? = null
    private var collectionReference: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_stickers_list)

        bindUiViews()
    }

    private fun bindUiViews() {
        firestore = FirebaseFirestore.getInstance()
        collectionReference = firestore?.collection(KEY_STICKER_PACK)

        dialogView = CustomDialogView(this)

        stickerPackList.adapter = stickerPacksAdapter
        fabAddNewStickers.setOnClickListener {
            it.avoidDoubleClicks()
            startCreateNewSticker()
        }

        getAllAvailableStickers()
    }

    private fun getAllAvailableStickers() {
        dialogView?.show()
        val query = collectionReference?.orderBy(KEY_CREATED_AT, Query.Direction.DESCENDING)
        query?.get()?.addOnCompleteListener { task ->
            dialogView?.dismiss()
            if (task.isComplete && task.isSuccessful) {
                val stickerPacks = ArrayList<StickerPack>()
                task.result?.forEach {
                    val sticker = it.toObject(StickerPack::class.java)
                    stickerPacks.add(sticker)
                }
                stickerPacksAdapter.setStickerPacks(stickerPacks)
            }
        }?.addOnFailureListener {
            dialogView?.dismiss()
        }
    }

    override fun onStickerPackClicked(stickerPack: StickerPack) {
        startStickerPackDetailsActivity(stickerPack)
    }
}