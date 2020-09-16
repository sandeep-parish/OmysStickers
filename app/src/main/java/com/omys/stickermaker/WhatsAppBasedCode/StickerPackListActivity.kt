package com.omys.stickermaker.WhatsAppBasedCode

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.shape.ShapeType
import co.mobiwise.materialintro.view.MaterialIntroView
import com.facebook.drawee.backends.pipeline.Fresco
import com.omys.stickermaker.*
import com.omys.stickermaker.WhatsAppBasedCode.StickerPackListAdapter.OnAddButtonClickedListener
import com.omys.stickermaker.app.StickerPackDetailsActivity
import com.omys.stickermaker.helpers.FirebaseHelper
import com.omys.stickermaker.helpers.OnUploadCallback
import com.omys.stickermaker.utils.TRAY_IMAGE
import kotlinx.android.synthetic.main.activity_sticker_pack_list.*
import java.util.*

class StickerPackListActivity : BaseActivity(), OnUploadCallback {

    private var firebaseHelper: FirebaseHelper? = null
    private val STICKER_PREVIEW_DISPLAY_LIMIT = 5
    private var allStickerPacksListAdapter: StickerPackListAdapter? = null
    private var newName: String? = null
    private var newCreator: String? = null

    private var packLayoutManager: LinearLayoutManager? = null
    var stickerPackList: ArrayList<StickerPack?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker_pack_list)

        StickerBook.init(this)
        Fresco.initialize(this)
        firebaseHelper = FirebaseHelper(this)

        stickerPackList = StickerBook.getAllStickerPacks()
        showStickerPackList(stickerPackList)
        fabAddNewStickers?.setOnClickListener { addNewStickerPackInInterface() }

        if (Intent.ACTION_SEND == intent.action) {
            val extras = intent.extras
            if (extras?.containsKey(Intent.EXTRA_STREAM) == true) {
                val uri = extras.getParcelable<Parcelable>(Intent.EXTRA_STREAM) as Uri?
                if (uri != null) {
                    DataArchiver.importZipFileToStickerPack(uri, this@StickerPackListActivity)
                }
            }
        }
        if (toShowIntro()) {
            startActivityForResult(Intent(this, NewUserIntroActivity::class.java), 1114)
        }
    }

    override fun onResume() {
        super.onResume()
        val action = intent.action
        if (action == null) {
            Log.v("Example", "Force restart")
            val intent = Intent(this, StickerPackListActivity::class.java)
            intent.action = "Already created"
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        DataArchiver.writeStickerBookJSON(StickerBook.getAllStickerPacks(), this)
    }

    override fun onDestroy() {
        DataArchiver.writeStickerBookJSON(StickerBook.getAllStickerPacks(), this)
        super.onDestroy()
    }

    private fun showStickerPackList(stickerPackList: List<StickerPack?>?) {
        allStickerPacksListAdapter = StickerPackListAdapter(stickerPackList!!, onAddButtonClickedListener)
        sticker_pack_list?.adapter = allStickerPacksListAdapter
        packLayoutManager = LinearLayoutManager(this)
        packLayoutManager?.orientation = LinearLayoutManager.VERTICAL
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        sticker_pack_list?.addItemDecoration(dividerItemDecoration)
        sticker_pack_list?.layoutManager = packLayoutManager
        sticker_pack_list?.viewTreeObserver?.addOnGlobalLayoutListener { recalculateColumnCount() }
    }

    private val onAddButtonClickedListener = OnAddButtonClickedListener { pack ->
        if (pack.stickers.size >= 3) {
            val intent = Intent()
            intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
            intent.putExtra(StickerPackDetailsActivityOld.EXTRA_STICKER_PACK_ID, pack.identifier)
            intent.putExtra(StickerPackDetailsActivityOld.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            intent.putExtra(StickerPackDetailsActivityOld.EXTRA_STICKER_PACK_NAME, pack.name)
            try {
                this@StickerPackListActivity.startActivityForResult(intent, StickerPackDetailsActivityOld.ADD_PACK)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@StickerPackListActivity, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show()
            }
        } else {
            val alertDialog = AlertDialog.Builder(this@StickerPackListActivity)
                    .setNegativeButton("Ok") { dialogInterface, i -> dialogInterface.dismiss() }.create()
            alertDialog.setTitle("Invalid Action")
            alertDialog.setMessage("In order to be applied to WhatsApp, the sticker pack must have at least 3 stickers. Please add more stickers first.")
            alertDialog.show()
        }
    }

    private fun recalculateColumnCount() {
        val previewSize = resources.getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size)
        val firstVisibleItemPosition = packLayoutManager!!.findFirstVisibleItemPosition()
        val viewHolder = sticker_pack_list?.findViewHolderForAdapterPosition(firstVisibleItemPosition) as StickerPackListItemViewHolder?
        if (viewHolder != null) {
            val max = Math.max(viewHolder.imageRowView.measuredWidth / previewSize, 1)
            val numColumns = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max)
            allStickerPacksListAdapter!!.setMaxNumberOfStickersInARow(numColumns)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == StickerPackDetailsActivityOld.ADD_PACK) {
            if (resultCode == RESULT_CANCELED && data != null) {
                val validationError = data.getStringExtra("validation_error")
                if (validationError != null) {
                    if (BuildConfig.DEBUG) {
                        //validation error should be shown to developer only, not users.
                        MessageDialogFragment.newInstance(R.string.title_validation_error, validationError).show(supportFragmentManager, "validation error")
                    }
                }
            }
        } else if (data != null && requestCode == 2319) {
            val uri = data.data
            uri?.let { contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            firebaseHelper?.uploadFile(uri, TRAY_IMAGE, this)
        } else if (requestCode == 1114) {
            makeIntroNotRunAgain()
            MaterialIntroView.Builder(this)
                    .enableIcon(false)
                    .setFocusGravity(FocusGravity.CENTER)
                    .setFocusType(Focus.MINIMUM)
                    .setDelayMillis(500)
                    .enableFadeAnimation(true)
                    .performClick(true)
                    .setInfoText("To add new sticker packs, click here.")
                    .setShape(ShapeType.CIRCLE)
                    .setTarget(findViewById(R.id.action_add))
                    .setUsageId("intro_card") //THIS SHOULD BE UNIQUE ID
                    .show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                val alertDialog = AlertDialog.Builder(this)
                        .setPositiveButton("Let's Go") { dialogInterface, i -> NewUserIntroActivity.verifyStoragePermissions(this@StickerPackListActivity) }
                        .create()
                alertDialog.setTitle("Notice!")
                alertDialog.setMessage("""
    We've recognized you denied the storage access permission for this app.

    In order for this app to work, storage access is required.
    """.trimIndent())
                alertDialog.show()
            }
        }
    }

    private fun addNewStickerPackInInterface() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Create New Sticker Pack")
        dialog.setMessage("Please specify title and creator for the pack.")
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val nameBox = EditText(this)
        nameBox.setLines(1)
        val buttonLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        buttonLayoutParams.setMargins(50, 0, 50, 10)
        nameBox.layoutParams = buttonLayoutParams
        nameBox.hint = "Pack Name"
        nameBox.inputType = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
        layout.addView(nameBox)
        val creatorBox = EditText(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            creatorBox.setAutofillHints("name")
        }
        creatorBox.setLines(1)
        creatorBox.layoutParams = buttonLayoutParams
        creatorBox.inputType = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
        creatorBox.hint = "Creator"
        layout.addView(creatorBox)
        dialog.setView(layout)
        dialog.setPositiveButton("OK", null)
        dialog.setNegativeButton("Cancel") { dialog, id -> dialog.cancel() }
        val ad = dialog.create()
        ad.show()
        val b = ad.getButton(AlertDialog.BUTTON_POSITIVE)
        b.setOnClickListener {
            if (TextUtils.isEmpty(nameBox.text)) {
                nameBox.error = "Package name is required!"
            }
            if (TextUtils.isEmpty(creatorBox.text)) {
                creatorBox.error = "Creator is required!"
            }
            if (!TextUtils.isEmpty(nameBox.text) && !TextUtils.isEmpty(creatorBox.text)) {
                ad.dismiss()
                createDialogForPickingIconImage(nameBox, creatorBox)
            }
        }
        creatorBox.setOnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                b.performClick()
            }
            false
        }
    }

    private fun createDialogForPickingIconImage(nameBox: EditText, creatorBox: EditText) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick your pack's icon image")
        builder.setMessage("Now you will pick the new sticker pack's icon image.")
                .setCancelable(false)
                .setPositiveButton("Let's go") { dialog, id ->
                    dialog.dismiss()
                    openFileTray(nameBox.text.toString(), creatorBox.text.toString())
                }
        val alert = builder.create()
        alert.show()
    }

    private fun createNewStickerPackAndOpenIt(name: String?, creator: String?, trayImage: String?) {
        val newId = UUID.randomUUID().toString()
        val sp = StickerPack(
                newId,
                name,
                creator,
                trayImage,
                "",
                "",
                "",
                "",
                this)
        StickerBook.addStickerPackExisting(sp)
        val intent = Intent(this, StickerPackDetailsActivity::class.java)
        intent.putExtra(StickerPackDetailsActivityOld.EXTRA_SHOW_UP_BUTTON, true)
        intent.putExtra(StickerPackDetailsActivityOld.EXTRA_STICKER_PACK_DATA, newId)
        intent.putExtra("isNewlyCreated", true)
        this.startActivity(intent)
    }

    private fun openFileTray(name: String, creator: String) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
        i.type = "image/*"
        newName = name
        newCreator = creator
        startActivityForResult(i, 2319)
    }

    private fun makeIntroNotRunAgain() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val previouslyStarted = prefs.getBoolean("isAlreadyShown", false)
        if (!previouslyStarted) {
            val edit = prefs.edit()
            edit.putBoolean("isAlreadyShown", false)
            edit.commit()
        }
    }

    private fun toShowIntro(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        return prefs.getBoolean("isAlreadyShown", true)
    }

    override fun onUploadComplete(identifier: String, fileUrl: String) {
        when (identifier) {
            TRAY_IMAGE -> {
                createNewStickerPackAndOpenIt(newName, newCreator, fileUrl)
            }
        }
    }
}