package com.omys.stickermaker.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.omys.stickermaker.R
import com.omys.stickermaker.adapter.StickerPacksListAdapter
import com.omys.stickermaker.database.OmysDatabase
import com.omys.stickermaker.helpers.OnStickerPackCallBack
import com.omys.stickermaker.helpers.StickerPackHelper
import com.omys.stickermaker.modal.StickerPackInfoModal
import com.omys.stickermaker.modal.StickerPackModal
import com.omys.stickermaker.utils.*
import kotlinx.android.synthetic.main.activity_all_stickers_list.*

class AllStickersListActivity : AppCompatActivity(), StickerPacksListAdapter.OnStickerPackAction, OnStickerPackCallBack {

    private val stickerPackHelper by lazy { StickerPackHelper(this) }
    private val progressDialog by lazy { CustomDialogView(this) }
    private val stickerPacksAdapter by lazy { StickerPacksListAdapter(this, this) }

    private var database: OmysDatabase? = null
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
        database = OmysDatabase.getDatabase(this)

        stickerPackList.adapter = stickerPacksAdapter

        if (IS_ADMIN_RIGHTS) {
            fabAddNewStickers.visible()
            fabAddNewStickers.setOnClickListener {
                it.avoidDoubleClicks()
                startCreateNewSticker()
            }
        }

        getAllAvailableStickers()
    }

    /**Get sticker packs from Fire Store */
    private fun getAllAvailableStickers() {
        val minimumSticker = if (IS_ADMIN_RIGHTS) 0 else 3
        progressDialog.show()
        val query = collectionReference//minimum 2 stickers required in a pack
                ?.whereGreaterThanOrEqualTo(KEY_TOTAL_STICKERS, minimumSticker)
        query?.get()?.addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isComplete && task.isSuccessful) {
                val stickerPacks = ArrayList<StickerPackInfoModal>()
                task.result?.forEach {
                    val sticker = it.toObject(StickerPackInfoModal::class.java)
                    stickerPacks.add(sticker)
                }
                stickerPacks.sortByDescending { it.createdAt }
                stickerPacksAdapter.setStickerPacks(stickerPacks)
            }
        }?.addOnFailureListener {
            progressDialog.dismiss()
        }
    }

    override fun onStickerPackDownload(stickerPackInfoModal: StickerPackInfoModal) {
        stickerPackHelper.startDownloadingStickerPack(stickerPackInfoModal, this);
    }

    override fun onStickerPackDownloaded(stickerPack: StickerPackModal?) {
        database?.stickerPacksDatabase()?.addNewStickerPack(stickerPack)

        addStickerPackToWhatsApp(stickerPack?.identifier, stickerPack?.name)

        val stickerPackIndex = stickerPacksAdapter.stickerPacksList.indexOfFirst { it.id == stickerPack?.identifier }
        stickerPacksAdapter.notifyItemChanged(stickerPackIndex)
    }
}