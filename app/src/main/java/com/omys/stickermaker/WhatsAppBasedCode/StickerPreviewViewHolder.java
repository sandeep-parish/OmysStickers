/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.omys.stickermaker.WhatsAppBasedCode;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.omys.stickermaker.R;

public class StickerPreviewViewHolder extends RecyclerView.ViewHolder {

    public SimpleDraweeView stickerPreviewView;

    StickerPreviewViewHolder(final View itemView) {
        super(itemView);
        stickerPreviewView = itemView.findViewById(R.id.sticker_preview);
    }
}