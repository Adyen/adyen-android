package com.adyen.checkout.ui.internal.card;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.base.TxVariantProvider;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.image.Rembrandt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 14/08/2017.
 */
public class LogoAdapter extends RecyclerView.Adapter<LogoAdapter.ImageViewHolder> {
    private final AppCompatActivity mActivity;

    private final RecyclerView mRecyclerView;

    private final LogoApi mLogoApi;

    private final List<TxVariantProvider> mTxVariantProviders;

    LogoAdapter(@NonNull AppCompatActivity activity, @NonNull RecyclerView recyclerView, @NonNull LogoApi logoApi) {
        mActivity = activity;
        mRecyclerView = recyclerView;
        mLogoApi = logoApi;
        mTxVariantProviders = new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Resources resources = parent.getResources();
        int width = resources.getDimensionPixelSize(R.dimen.payment_method_logo_width);
        int height = resources.getDimensionPixelSize(R.dimen.payment_method_logo_height);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(width, height);

        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(layoutParams);

        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        TxVariantProvider txVariantProvider = mTxVariantProviders.get(position);
        Callable<Drawable> logoCallable = mLogoApi.newBuilder(txVariantProvider).buildCallable();
        Rembrandt.createDefaultLogoRequestArgs(mActivity.getApplication(), logoCallable).into(mActivity, holder, holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mTxVariantProviders.size();
    }

    public void setTxVariantProviders(@NonNull final List<? extends TxVariantProvider> txVariantProviders) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                List<TxVariantProvider> oldTxVariantProviders = new ArrayList<>(mTxVariantProviders);
                mTxVariantProviders.clear();
                mTxVariantProviders.addAll(txVariantProviders);

                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(oldTxVariantProviders, mTxVariantProviders));
                diffResult.dispatchUpdatesTo(LogoAdapter.this);

                mRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    static final class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mImageView;

        private ImageViewHolder(@NonNull ImageView imageView) {
            super(imageView);

            mImageView = imageView;
        }
    }

    private final class DiffCallback extends DiffUtil.Callback {
        private final List<TxVariantProvider> mOldTxVariantProviders;

        private final List<TxVariantProvider> mNewTxVariantProviders;

        private DiffCallback(@NonNull List<TxVariantProvider> oldTxVariantProviders, @NonNull List<TxVariantProvider> newTxVariantProviders) {
            mOldTxVariantProviders = oldTxVariantProviders;
            mNewTxVariantProviders = newTxVariantProviders;
        }

        @Override
        public int getOldListSize() {
            return mOldTxVariantProviders.size();
        }

        @Override
        public int getNewListSize() {
            return mNewTxVariantProviders.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            TxVariantProvider oldPaymentMethod = mOldTxVariantProviders.get(oldItemPosition);
            TxVariantProvider newPaymentMethod = mNewTxVariantProviders.get(newItemPosition);

            return oldPaymentMethod.getTxVariant().equals(newPaymentMethod.getTxVariant());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return true;
        }
    }
}
