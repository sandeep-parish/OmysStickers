package com.omys.stickerapp.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.omys.stickerapp.R
import com.omys.stickerapp.adapter.StickerPacksListAdapter
import com.omys.stickerapp.database.OmysDatabase
import com.omys.stickerapp.database.StickerPacksDao
import com.omys.stickerapp.helpers.OnStickerPackCallBack
import com.omys.stickerapp.helpers.StickerPackHelper
import com.omys.stickerapp.modal.StickerPackInfoModal
import com.omys.stickerapp.modal.StickerPackModal
import com.omys.stickerapp.utils.*
import kotlinx.android.synthetic.main.activity_all_stickers_list.*
import kotlinx.android.synthetic.main.activity_sticker_pack_details.*


fun Context.startAllStickersActivity(topActivity: Boolean = false) {
    val intent = Intent(this, AllStickersListActivity::class.java)
    if (topActivity) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK;
    }
    startActivity(intent)
}

class AllStickersListActivity : AppCompatActivity(), StickerPacksListAdapter.OnStickerPackAction, OnStickerPackCallBack {

    private val stickerPackHelper by lazy { StickerPackHelper(this) }
    private val progressDialog by lazy { CustomDialogView(this) }
    private val stickerPacksAdapter by lazy { StickerPacksListAdapter(this, this) }

    private var stickerPacksDb: StickerPacksDao? = null
    private var firestore: FirebaseFirestore? = null
    private var collectionReference: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_stickers_list)

        bindUiViews()
    }

    private fun bindUiViews() {
        firestore = FirebaseFirestore.getInstance()
        collectionReference = firestore?.collection(STICKER_PACKS_COLLECTION)
        stickerPacksDb = OmysDatabase.getDatabase(this).stickerPacksDatabase()

        stickerPackList.adapter = stickerPacksAdapter
        stickerPacksDb
                ?.getOfflineStickerPacks(3)
                ?.observe(this, Observer {
                    stickerPacksAdapter.setStickerPacks(it)
                })

        if (IS_ADMIN_RIGHTS) {
            fabAddNewStickers.visible()
            fabAddNewStickers.setOnClickListener {
                it.avoidDoubleClicks()
                startCreateNewSticker()
            }
        }

        val lastUpdatedInterval = System.currentTimeMillis() - UserPrefs.getLastUpdated(this)
        if (lastUpdatedInterval >= REFRESH_INTERVAL) {
            getAllAvailableStickers()
        } else {
            showTimedLoading()
        }
    }

    /**Get sticker packs from Fire Store */
    private fun getAllAvailableStickers() {
        progressDialog.show()

        val lastAddedStickerPack = stickerPacksDb?.getLastAddedStickerPack()
                ?: StickerPackInfoModal()
        val query = collectionReference?.whereGreaterThan(KEY_CREATED_AT, lastAddedStickerPack.createdAt)

        query?.get()?.addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isComplete && task.isSuccessful) {
                task.result?.forEach {
                    val sticker = it.toObject(StickerPackInfoModal::class.java)
                    stickerPacksDb?.addNewStickerPackLocally(sticker)
                }
                UserPrefs.saveLastUpdated(System.currentTimeMillis(), this)
                debugToast("Fetched ${task.result.size()} from online source")
            }
        }?.addOnFailureListener {
            progressDialog.dismiss()
        }
    }

    override fun onStickerPackAction(actionType: String, stickerPackInfoModal: StickerPackInfoModal) {
        if (actionType.equals(getString(R.string.share).trim(), true)) {
            shareStickerPack(stickerPackInfoModal.id, stickerPackInfoModal.name)
        } else if (actionType.equals(getString(R.string.add).trim(), true)) {
            stickerPackHelper.startDownloadingStickerPack(stickerPackInfoModal, this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ADD_STICKER_PACK_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    showToast("Sticker pack added successfully")
                    viewAlreadyAdded.visible()
                    btnAddToWhatsApp.hide()
                } else {
                    val validationError = data?.getStringExtra("validation_error")
                    debugPrint(validationError.toString())
                }
            }
        }
    }

    override fun onStickerPackDownloaded(stickerPack: StickerPackModal?) {
        stickerPacksDb?.addNewStickerPack(stickerPack)

        addStickerPackToWhatsApp(stickerPack?.identifier, stickerPack?.name)

        val stickerPackIndex = stickerPacksAdapter.stickerPacksList.indexOfFirst { it.id == stickerPack?.identifier }
        stickerPacksAdapter.notifyItemChanged(stickerPackIndex)
    }
}