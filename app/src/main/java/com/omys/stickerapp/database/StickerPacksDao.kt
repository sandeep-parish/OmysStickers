package com.omys.stickerapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omys.stickerapp.modal.StickerPackInfoModal
import com.omys.stickerapp.modal.StickerPackModal

@Dao
interface StickerPacksDao {

    @Query("SELECT * FROM stickerPacksModal")
    fun getAllStickerPacks(): List<StickerPackModal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNewStickerPack(stickerPack: StickerPackModal?)

    @Query("SELECT * FROM stickerPacksModal where identifier=:packId LIMIT 1")
    fun getStickerPackById(packId: String?): StickerPackModal?


    //Firebase sticker Packs Modal
    @Query("SELECT * FROM firebaseStickerPacks where totalStickers >=:minimumStickerLimit ORDER BY createdAt DESC")
    fun getOfflineStickerPacks(minimumStickerLimit: Int): LiveData<List<StickerPackInfoModal>>

    @Query("SELECT * FROM firebaseStickerPacks ORDER BY createdAt DESC LIMIT 1")
    fun getLastAddedStickerPack(): StickerPackInfoModal?

    @Query("SELECT * FROM firebaseStickerPacks where id=:packId ORDER BY createdAt DESC LIMIT 1")
    fun getOfflineStickerPackById(packId: String?): StickerPackInfoModal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNewStickerPackLocally(stickerPack: StickerPackInfoModal?)
}