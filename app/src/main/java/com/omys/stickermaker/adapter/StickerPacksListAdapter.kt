package com.omys.stickermaker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omys.stickermaker.R
import com.omys.stickermaker.modal.StickerPack
import com.omys.stickermaker.utils.loadImage
import kotlinx.android.synthetic.main.list_item_all_sticker_pack.view.*

class StickerPacksListAdapter(private val onAction: OnStickerPackAction) : RecyclerView.Adapter<StickerPacksListAdapter.ReportItemViewHolder>() {

    private val stickerPacksList = ArrayList<StickerPack>()

    fun setStickerPacks(dataList: List<StickerPack>) {
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

    inner class ReportItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(dataBean: StickerPack) {
            with(dataBean) {
                itemView.sticker_pack_title?.text = name
                itemView.sticker_pack_publisher.text = "by " + publisher + " - " + stickers.size + " Stickers"
                itemView.stickerTrayImage.loadImage(tray_image_file.toString())

                if (!stickers.isNullOrEmpty()) {
                    val stickersAdapter = StickersListAdapter()
                    itemView.availableStickersList.adapter = stickersAdapter
                    stickersAdapter.setStickerPacks(stickers)
                }

                itemView.setOnClickListener { onAction.onStickerPackClicked(this) }
            }
        }
    }


    interface OnStickerPackAction {
        fun onStickerPackClicked(stickerPack: StickerPack)
    }
}
