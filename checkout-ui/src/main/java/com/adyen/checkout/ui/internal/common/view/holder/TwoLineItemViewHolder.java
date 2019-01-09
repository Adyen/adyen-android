/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/08/2017.
 */

package com.adyen.checkout.ui.internal.common.view.holder;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adyen.checkout.ui.R;

public final class TwoLineItemViewHolder extends RecyclerView.ViewHolder {
    private ImageView mLogoImageView;

    private TextView mPrimaryTextView;

    private TextView mSecondaryTextView;

    private ImageView mActionImageView;

    @NonNull
    public static TwoLineItemViewHolder create(@NonNull ViewGroup parent) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_two_line, parent, false);

        return new TwoLineItemViewHolder(itemView);
    }

    @NonNull
    public static TwoLineItemViewHolder create(@NonNull View parent, @IdRes int includeId) {
        View itemView = parent.findViewById(includeId);

        return new TwoLineItemViewHolder(itemView);
    }

    private TwoLineItemViewHolder(@NonNull View itemView) {
        super(itemView);

        mLogoImageView = itemView.findViewById(R.id.imageView_logo);
        mPrimaryTextView = itemView.findViewById(R.id.textView_primary);
        mSecondaryTextView = itemView.findViewById(R.id.textView_secondary);
        mActionImageView = itemView.findViewById(R.id.imageView_action);
    }

    @NonNull
    public ImageView getLogoImageView() {
        return mLogoImageView;
    }

    @NonNull
    public TextView getPrimaryTextView() {
        return mPrimaryTextView;
    }

    @NonNull
    public TextView getSecondaryTextView() {
        return mSecondaryTextView;
    }

    @NonNull
    public ImageView getActionImageView() {
        return mActionImageView;
    }

    public void setPrimaryText(@Nullable CharSequence primaryText) {
        if (TextUtils.isEmpty(primaryText)) {
            mPrimaryTextView.setVisibility(View.GONE);
        } else {
            mPrimaryTextView.setVisibility(View.VISIBLE);
        }

        mPrimaryTextView.setText(primaryText);
    }

    public void setSecondaryText(@Nullable CharSequence secondaryText) {
        if (TextUtils.isEmpty(secondaryText)) {
            mSecondaryTextView.setVisibility(View.GONE);
        } else {
            mSecondaryTextView.setVisibility(View.VISIBLE);
        }

        mSecondaryTextView.setText(secondaryText);
    }
}
