package com.omys.stickerapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omys.stickerapp.modal.StickerPackModal

@Dao
interface StickerPacksDao {

    @Query("SELECT * FROM stickerPacksModal")
    fun getAllStickerPacks(): List<StickerPackModal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNewStickerPack(stickerPack: StickerPackModal?)

    @Query("SELECT * FROM stickerPacksModal where identifier=:packId LIMIT 1")
    fun getStickerPackById(packId: String?): StickerPackModal?
}