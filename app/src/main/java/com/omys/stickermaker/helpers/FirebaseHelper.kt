package com.omys.stickermaker.helpers

import android.app.Activity
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.omys.stickermaker.modal.StickerPack
import com.omys.stickermaker.utils.*

class FirebaseHelper(private val activity: Activity?) {

    var progressDialog: CustomDialogView? = null
    var storageRef: StorageReference? = null
    var firestore: FirebaseFirestore? = null

    init {
        storageRef = Firebase.storage.reference
        firestore = FirebaseFirestore.getInstance()
        progressDialog = CustomDialogView(activity)
    }

    fun createNewStickerPack(stickerPack: StickerPack?, callback: OnSickerPackCallback? = null) {
        stickerPack ?: return
        progressDialog?.show()
        val collectionReference = firestore?.collection(KEY_STICKER_PACK)
        stickerPack.identifier = collectionReference?.document()?.id.toString()

        collectionReference?.document(stickerPack.identifier.toString())
                ?.set(stickerPack)?.addOnCompleteListener { it ->
                    progressDialog?.dismiss()
                    if (it.isComplete && it.isSuccessful) {
                        callback?.onPackCreated(stickerPack)
                    } else {
                        callback?.onPackCreated(null)
                    }
                }
    }

    fun uploadFile(fileUri: Uri?, identifier: String, callback: OnUploadCallback? = null, directoryName: String = DIR_STICKERS) {
        fileUri ?: return
        progressDialog?.show()
        val fileExtension = activity?.getFileExtensionFromUri(uri = fileUri)
        val fileName = "OMYS_IMG${System.currentTimeMillis()}$fileExtension"
        val fileReference = storageRef?.child("$directoryName$fileName")
        val uploadTask = fileReference?.putFile(fileUri)

        uploadTask
                ?.addOnSuccessListener {
                    fileReference.downloadUrl.addOnCompleteListener {
                        progressDialog?.dismiss()
                        if (it.isComplete && it.isSuccessful) {
                            debugPrint("File Upload successfully located at : ${it.result.toString()}")
                            callback?.onUploadComplete(identifier, it.result.toString())
                        }
                    }
                }
                ?.addOnFailureListener {
                    progressDialog?.dismiss()
                }
    }

    fun updateStickerPackData(stickerPackId: String?, data: Map<String, Any>, onCallback: OnStickerPackUpdate? = null) {
        stickerPackId ?: return
        progressDialog?.show()
        val query = firestore?.collection(KEY_STICKER_PACK)?.document(stickerPackId.toString())
        query?.update(data)
                ?.addOnCompleteListener {
                    progressDialog?.dismiss()
                    onCallback?.onPackUpdated(it.isSuccessful)
                    debugPrint("Sticker pack $stickerPackId Updated")
                }
    }
}

interface OnStickerPackUpdate {
    fun onPackUpdated(isSuccessful: Boolean)
}

interface OnSickerPackCallback {
    fun onPackCreated(stickerPack: StickerPack?)
}

interface OnUploadCallback {
    fun onUploadComplete(identifier: String, fileUrl: String)
}
