package com.omys.stickermaker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omys.stickermaker.R
import com.omys.stickermaker.utils.avoidDoubleClicks
import com.omys.stickermaker.utils.loadImage
import kotlinx.android.synthetic.main.item_sticker.view.*

class StickersListAdapter(private val onStickerClick: OnStickerClick? = null, private var layoutResource: Int = 0) : RecyclerView.Adapter<StickersListAdapter.ReportItemViewHolder>() {

    val stickers = ArrayList<String>()

    fun setStickerPacks(dataList: ArrayList<String>?) {
        dataList ?: return
        stickers.clear()
        stickers.addAll(dataList)
        notifyDataSetChanged()
    }

    fun addNewSticker(stickerUri: String) {
        stickers.add(0, stickerUri)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportItemViewHolder {
        if (layoutResource == 0) {
            layoutResource = R.layout.item_sticker
        }
        val itemView = LayoutInflater.from(parent.context)
                .inflate(layoutResource, parent, false)
        return ReportItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReportItemViewHolder, position: Int) {
        holder.bindItem(stickers[position])
    }

    override fun getItemCount(): Int {
        return stickers.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ReportItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(stickerUri: String) {
            itemView.stickerView?.loadImage(stickerUri)
            itemView.setOnClickListener {
                it.avoidDoubleClicks()
                onStickerClick?.onStickerClick(stickerUri)
            }
        }
    }

    interface OnStickerClick {
        fun onStickerClick(any: Any)
    }
}
