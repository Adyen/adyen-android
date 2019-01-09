/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 01/05/2018.
 */

package com.adyen.checkout.ui.internal.common.util.image;

import android.arch.lifecycle.Lifecycle;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

class ViewHolderRequest extends LifecycleAwareRequest {
    private RecyclerView.ViewHolder mViewHolder;

    private Target mTarget;

    private int mTargetAdapterPosition;

    ViewHolderRequest(
            @NonNull Rembrandt rembrandt,
            @NonNull RequestArgs requestArgs,
            @NonNull Lifecycle lifecycle,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull Target target
    ) {
        super(rembrandt, requestArgs, lifecycle);

        mViewHolder = viewHolder;
        mTarget = target;
        mTargetAdapterPosition = viewHolder.getAdapterPosition();
    }

    @Override
    boolean isCancelled() {
        return super.isCancelled() || !isValidAdapterPosition();
    }

    @Override
    void onDrawableLoaded(@Nullable Drawable drawable) {
        if (mTarget != null) {
            mTarget.setImageDrawable(drawable);
        }
    }

    private boolean isValidAdapterPosition() {
        int currentAdapterPosition = mViewHolder.getAdapterPosition();

        if (mTargetAdapterPosition == RecyclerView.NO_POSITION) {
            if (currentAdapterPosition != RecyclerView.NO_POSITION) {
                mTargetAdapterPosition = currentAdapterPosition;
            }

            return true;
        }

        return mTargetAdapterPosition == currentAdapterPosition;
    }
}
