/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/08/2017.
 */

package com.adyen.checkout.ui.internal.issuer;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.IssuerDetails;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.internal.common.util.image.Rembrandt;
import com.adyen.checkout.ui.internal.common.view.holder.TwoLineItemViewHolder;

import java.util.List;
import java.util.concurrent.Callable;

class IssuersAdapter extends RecyclerView.Adapter<TwoLineItemViewHolder> {
    private final AppCompatActivity mActivity;

    private final PaymentMethod mPaymentMethod;

    private final LogoApi mLogoApi;

    private final List<Item> mItems;

    private Listener mListener;

    IssuersAdapter(@NonNull AppCompatActivity activity, @NonNull PaymentMethod paymentMethod, @NonNull LogoApi logoApi, @NonNull Listener listener) {
        mActivity = activity;
        mPaymentMethod = paymentMethod;
        mLogoApi = logoApi;
        mItems = InputDetailImpl.findByKey(paymentMethod.getInputDetails(), IssuerDetails.KEY_ISSUER).getItems();
        mListener = listener;
    }

    @NonNull
    @Override
    public TwoLineItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TwoLineItemViewHolder holder = TwoLineItemViewHolder.create(parent);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = getItem(holder.getAdapterPosition());

                if (item != null) {
                    mListener.onIssuerClick(mPaymentMethod, item);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TwoLineItemViewHolder holder, int position) {
        Item item = getItem(position);

        if (item != null) {
            Application application = mActivity.getApplication();
            Callable<Drawable> logoCallable = mLogoApi
                    .newBuilder(mPaymentMethod)
                    .setTxSubVariantProvider(item)
                    .buildCallable();
            Rembrandt.createDefaultLogoRequestArgs(application, logoCallable).into(mActivity, holder, holder.getLogoImageView());
            holder.setPrimaryText(item.getName());
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Nullable
    private Item getItem(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position) : null;
    }

    interface Listener {
        void onIssuerClick(@NonNull PaymentMethod paymentMethod, @NonNull Item item);
    }
}
