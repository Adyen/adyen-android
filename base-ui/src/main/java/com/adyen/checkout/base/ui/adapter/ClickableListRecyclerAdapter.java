/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/5/2019.
 */

package com.adyen.checkout.base.ui.adapter;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

public abstract class ClickableListRecyclerAdapter<ViewHolderT extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolderT> {
    static final String TAG = LogUtil.getTag();

    OnItemCLickedListener mOnItemCLickedListener;

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull final ViewHolderT viewHolderT, int position) {
        viewHolderT.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d(TAG, "click");
                if (mOnItemCLickedListener != null) {
                    mOnItemCLickedListener.onItemClicked(viewHolderT.getAdapterPosition());
                }
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
