/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/5/2019.
 */

package com.adyen.checkout.issuerlist;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.ui.adapter.ClickableListRecyclerAdapter;
import com.adyen.checkout.base.ui.view.RoundCornerImageView;
import com.adyen.checkout.issuerlist.ui.R;

import java.util.List;

class IssuerListRecyclerAdapter extends ClickableListRecyclerAdapter<IssuerListRecyclerAdapter.IssuerViewHolder> {
    private List<IssuerModel> mIssuerModelList;
    private final ImageLoader mImageLoader;
    private final String mPaymentMethod;
    private final boolean mHideIssuerLogo;

    IssuerListRecyclerAdapter(@NonNull List<IssuerModel> issuerModelList, ImageLoader imageLoader, String paymentMethod, boolean hideIssuerLogo) {
        mIssuerModelList = issuerModelList;
        mHideIssuerLogo = hideIssuerLogo;
        mImageLoader = imageLoader;
        mPaymentMethod = paymentMethod;
    }

    @NonNull
    @Override
    public IssuerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_list_with_image, viewGroup, false);
        return new IssuerViewHolder(view, mHideIssuerLogo);
    }

    @Override
    public void onBindViewHolder(@NonNull IssuerViewHolder issuerViewHolder, int position) {
        super.onBindViewHolder(issuerViewHolder, position);
        issuerViewHolder.bind(mPaymentMethod, mIssuerModelList.get(position), mHideIssuerLogo, mImageLoader);
    }

    @Override
    public int getItemCount() {
        return mIssuerModelList.size();
    }

    // We only expect either a full new list or small changes on an item, like a new Logo drawable
    void updateIssuerModelList(@NonNull List<IssuerModel> issuerModelList) {
        mIssuerModelList = issuerModelList;
        notifyDataSetChanged();
    }

    IssuerModel getIssuerAt(int position) {
        return mIssuerModelList.get(position);
    }

    final class IssuerViewHolder extends RecyclerView.ViewHolder {

        private final RoundCornerImageView mLogoImage;
        private final TextView mText;

        IssuerViewHolder(@NonNull View itemView, boolean hideIssuerLogo) {
            super(itemView);
            mLogoImage = itemView.findViewById(R.id.imageView_logo);
            mText = itemView.findViewById(R.id.textView_text);

            mLogoImage.setVisibility(hideIssuerLogo ? View.GONE : View.VISIBLE);
        }

        void bind(String paymentMethod, IssuerModel issuerModel, boolean hideIssuerLogo, ImageLoader imageLoader) {
            mText.setText(issuerModel.getName());
            if (!hideIssuerLogo) {
                imageLoader.load(paymentMethod,
                        issuerModel.getId(),
                        mLogoImage,
                        R.drawable.ic_placeholder_image,
                        R.drawable.ic_placeholder_image);
            }
        }

    }

}
