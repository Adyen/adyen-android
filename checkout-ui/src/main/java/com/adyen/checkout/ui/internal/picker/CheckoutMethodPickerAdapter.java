package com.adyen.checkout.ui.internal.picker;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodPickerListener;
import com.adyen.checkout.ui.internal.common.util.SnackbarSwipeHandler;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;
import com.adyen.checkout.ui.internal.common.util.recyclerview.SimpleDiffCallback;
import com.adyen.checkout.ui.internal.common.view.holder.TwoLineItemViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 20/03/2018.
 */
class CheckoutMethodPickerAdapter extends RecyclerView.Adapter<TwoLineItemViewHolder> {
    private static final int VIEW_TYPE_DEFAULT = 0;

    private static final int VIEW_TYPE_DELETABLE = VIEW_TYPE_DEFAULT + 1;

    private final LifecycleOwner mLifecycleOwner;

    private final LogoApi mLogoApi;

    private final CheckoutMethodPickerListener mListener;

    private List<CheckoutMethod> mAllCheckoutMethods;

    private RecyclerView mRecyclerView;

    CheckoutMethodPickerAdapter(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull LogoApi logoApi,
            @NonNull CheckoutMethodPickerListener listener
    ) {
        mLifecycleOwner = lifecycleOwner;
        mLogoApi = logoApi;
        mListener = listener;
        mAllCheckoutMethods = new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public TwoLineItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final TwoLineItemViewHolder holder = TwoLineItemViewHolder.create(parent);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();

                if (adapterPosition != RecyclerView.NO_POSITION) {
                    CheckoutMethod checkoutMethod = mAllCheckoutMethods.get(adapterPosition);
                    mListener.onCheckoutMethodSelected(checkoutMethod);
                }
            }
        });

        if (viewType == VIEW_TYPE_DELETABLE) {
            ImageView actionImageView = holder.getActionImageView();
            actionImageView.setImageResource(R.drawable.ic_clear_24dp);
            ThemeUtil.setTintFromAttributeColor(actionImageView.getContext(), actionImageView.getDrawable(), R.attr.colorIconActive);
            TypedArray typedArray = actionImageView.getContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
            actionImageView.setBackground(typedArray.getDrawable(0));
            typedArray.recycle();
            actionImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = holder.getAdapterPosition();

                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        CheckoutMethod checkoutMethod = mAllCheckoutMethods.get(adapterPosition);
                        requestShopperConfirmationForCheckoutMethodDeletion(checkoutMethod);
                    }
                }
            });
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TwoLineItemViewHolder holder, int position) {
        CheckoutMethod checkoutMethod = mAllCheckoutMethods.get(position);
        checkoutMethod.buildLogoRequestArgs(mLogoApi).into(mLifecycleOwner, holder, holder.getLogoImageView());
        holder.setPrimaryText(checkoutMethod.getPrimaryText());
        holder.setSecondaryText(checkoutMethod.getSecondaryText());
        holder.itemView.setTag(checkoutMethod.getPaymentMethod().getType());
    }

    @Override
    public int getItemCount() {
        return mAllCheckoutMethods.size();
    }

    @Override
    public int getItemViewType(int position) {
        CheckoutMethod checkoutMethod = mAllCheckoutMethods.get(position);

        if (mListener.isCheckoutMethodDeletable(checkoutMethod)) {
            return VIEW_TYPE_DELETABLE;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    void updateCheckoutMethods(@NonNull List<CheckoutMethod> checkoutMethods) {
        DiffUtil.DiffResult diffResult = DiffUtil
                .calculateDiff(new SimpleDiffCallback<>(mAllCheckoutMethods, checkoutMethods));
        diffResult.dispatchUpdatesTo(CheckoutMethodPickerAdapter.this);
        mAllCheckoutMethods = checkoutMethods;
    }

    private void requestShopperConfirmationForCheckoutMethodDeletion(@NonNull final CheckoutMethod checkoutMethod) {
        Context context = mRecyclerView.getContext();

        String message = context.getString(R.string.checkout_one_click_delete_confirmation_message, checkoutMethod.getPrimaryText());

        Snackbar snackbar = Snackbar
                .make(mRecyclerView, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.checkout_one_click_delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onCheckoutMethodDelete(checkoutMethod);
                    }
                });
        snackbar.show();
        SnackbarSwipeHandler.attach(context, snackbar);
    }
}
