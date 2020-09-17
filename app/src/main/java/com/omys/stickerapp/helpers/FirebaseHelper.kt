package com.omys.stickerapp.helpers

import android.app.Activity
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.omys.stickerapp.modal.StickerPackInfoModal
import com.omys.stickerapp.utils.*
import java.io.File

class FirebaseHelper(private val activity: Activity?) {

    var progressDialog: CustomDialogView? = null
    var circularProgress: CircularProgress? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var firestore: FirebaseFirestore? = null

    init {
        firebaseStorage = Firebase.storage
        firestore = FirebaseFirestore.getInstance()
        progressDialog = CustomDialogView(activity)
        circularProgress = CircularProgress(activity)
    }

    fun createNewStickerPack(stickerPackInfoModal: StickerPackInfoModal?, callback: OnSickerPackCallback? = null) {
        stickerPackInfoModal ?: return
        progressDialog?.show()
        val collectionReference = firestore?.collection(KEY_STICKER_PACK)
        stickerPackInfoModal.id = collectionReference?.document()?.id.toString()

        collectionReference?.document(stickerPackInfoModal.id.toString())
                ?.set(stickerPackInfoModal)?.addOnCompleteListener { it ->
                    progressDialog?.dismiss()
                    if (it.isComplete && it.isSuccessful) {
                        callback?.onPackCreated(stickerPackInfoModal)
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
        val fileReference = firebaseStorage?.reference?.child("$directoryName$fileName")
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

    fun downloadFileFromFirebaseUrl(fileUrl: String?, directory: String?, type: String, callback: OnFileDownload? = null) {
        fileUrl ?: return

        val storageRef = firebaseStorage?.getReferenceFromUrl(fileUrl)
        val outputFile = File(directory, storageRef?.name.toString())
        if (outputFile.exists()) outputFile.delete()

        storageRef?.getFile(outputFile)?.addOnSuccessListener {
            callback?.onFileDownloaded(outputFile, type)
            debugPrint("File downloaded at ${outputFile.absolutePath}")
        }?.addOnFailureListener {
            callback?.onFileDownloaded(null, type)
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

interface OnFileDownload {
    fun onFileDownloaded(file: File?, type: String)
}

interface OnStickerPackUpdate {
    fun onPackUpdated(isSuccessful: Boolean)
}

interface OnSickerPackCallback {
    fun onPackCreated(stickerPackInfoModal: StickerPackInfoModal?)
}

interface OnUploadCallback {
    fun onUploadComplete(identifier: String, fileUrl: String)
}
