package com.omys.stickerapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omys.stickerapp.R
import com.omys.stickerapp.app.startStickerPackDetailsActivity
import com.omys.stickerapp.database.OmysDatabase
import com.omys.stickerapp.database.StickerPacksDao
import com.omys.stickerapp.modal.StickerPackInfoModal
import com.omys.stickerapp.utils.loadImage
import com.omys.stickerapp.wahelper.WhiteListHelper
import kotlinx.android.synthetic.main.list_item_all_sticker_pack.view.*

class StickerPacksListAdapter(private val context: Context, private val onAction: OnStickerPackAction) : RecyclerView.Adapter<StickerPacksListAdapter.ReportItemViewHolder>() {
    private var stickerPacksDatabase: StickerPacksDao? = null

    init {
        stickerPacksDatabase = OmysDatabase.getDatabase(context).stickerPacksDatabase()
    }

    val stickerPacksList = ArrayList<StickerPackInfoModal>()

    fun setStickerPacks(dataList: List<StickerPackInfoModal>) {
        stickerPacksList.clear()
        stickerPacksList.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_all_sticker_pack, parent, false)
        return ReportItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReportItemViewHolder, position: Int) {
        holder.bindItem(stickerPacksList[position])
    }

    override fun getItemCount(): Int {
        return stickerPacksList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ReportItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), StickersListAdapter.OnStickerClick {
        fun bindItem(dataBean: StickerPackInfoModal) {
            with(dataBean) {
                itemView.sticker_pack_title?.text = name
                itemView.sticker_pack_publisher.text = "By " + publisher?.capitalize() + " - " + stickers.size + " Stickers"
                itemView.stickerTrayImage.loadImage(tray_image_file.toString())

                if (!stickers.isNullOrEmpty()) {
                    val stickersAdapter = StickersListAdapter(this@ReportItemViewHolder, R.layout.item_sticker_small)
                    itemView.availableStickersList.adapter = stickersAdapter
                    stickersAdapter.setStickerPacks(stickers)
                }

                if (stickerPacksDatabase?.getStickerPackById(id) != null || WhiteListHelper.isWhitelisted(context, id.toString())) {
                    itemView.btnAddStickerPack.text = context.getString(R.string.share)
                } else {
                    itemView.btnAddStickerPack.text = context.getString(R.string.add)
                    itemView.btnAddStickerPack?.setOnClickListener { onAction.onStickerPackDownload(this) }
                }

                itemView.setOnClickListener {
                    context.startStickerPackDetailsActivity(this)
                }
            }
        }

        override fun onStickerClick(any: Any) {
            itemView.performClick()
        }
    }


    interface OnStickerPackAction {
        fun onStickerPackDownload(stickerPackInfoModal: StickerPackInfoModal)
    }
}
