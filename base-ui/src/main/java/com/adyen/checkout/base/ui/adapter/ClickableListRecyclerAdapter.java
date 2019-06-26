/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/5/2019.
 */

package com.adyen.checkout.base.ui.adapter;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

public abstract class ClickableListRecyclerAdapter<ViewHolderT extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolderT> {
    private static final String TAG = LogUtil.getTag();

    OnItemCLickedListener mOnItemCLickedListener;

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull ViewHolderT viewHolderT, int position) {
        viewHolderT.itemView.setOnClickListener(v -> {
            Logger.d(TAG, "click");
            if (mOnItemCLickedListener != null) {
                mOnItemCLickedListener.onItemClicked(viewHolderT.getAdapterPosition());
            }
        });
    }

    public void setItemCLickListener(@NonNull OnItemCLickedListener itemCLickListener) {
        mOnItemCLickedListener = itemCLickListener;
    }

    public interface OnItemCLickedListener {
        void onItemClicked(int position);
    }
}
