/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/5/2019.
 */

package com.adyen.checkout.issuerlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adyen.checkout.base.ui.adapter.ClickableListRecyclerAdapter;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.issuerlist.ui.R;

import java.util.List;

class IssuerListRecyclerAdapter extends ClickableListRecyclerAdapter<IssuerListRecyclerAdapter.IssuerViewHolder> {
    private static final String TAG = LogUtil.getTag();

    private List<IssuerModel> mIssuerModelList;
    private boolean mHideIssuerLogo;

    IssuerListRecyclerAdapter(@NonNull List<IssuerModel> issuerModelList) {
        this(issuerModelList, false);
    }

    IssuerListRecyclerAdapter(@NonNull List<IssuerModel> issuerModelList, boolean hideIssuerLogo) {
        mIssuerModelList = issuerModelList;
        mHideIssuerLogo = hideIssuerLogo;
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
        issuerViewHolder.bind(mIssuerModelList.get(position), mHideIssuerLogo);
    }

    @Override
    public int getItemCount() {
        return mIssuerModelList.size();
    }

    // We only expect either a full new list or small changes on an item, like a new Logo drawable
    void updateIssuerModelList(@NonNull List<IssuerModel> issuerModelList) {
        final boolean newList = mIssuerModelList.size() != issuerModelList.size();
        mIssuerModelList = issuerModelList;
        if (newList) {
            Logger.d(TAG, "new list");
            notifyDataSetChanged();
        } else {
            Logger.v(TAG, "update list");
            for (int position = 0; position < mIssuerModelList.size(); position++) {
                if (mIssuerModelList.get(position).isUpdated()) {
                    mIssuerModelList.get(position).consumeUpdate();
                    notifyItemChanged(position);
                }
            }
        }
    }

    IssuerModel getIssuerAt(int position) {
        return mIssuerModelList.get(position);
    }

    static final class IssuerViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mLogoImage;
        private final TextView mText;

        IssuerViewHolder(@NonNull View itemView, boolean hideIssuerLogo) {
            super(itemView);
            mLogoImage = itemView.findViewById(R.id.imageView_logo);
            mText = itemView.findViewById(R.id.textView_text);

            mLogoImage.setVisibility(hideIssuerLogo ? View.GONE : View.VISIBLE);
        }

        void bind(IssuerModel issuerModel, boolean hideIssuerLogo) {
            mText.setText(issuerModel.getName());
            if (!hideIssuerLogo) {
                if (issuerModel.getLogo() != null) {
                    mLogoImage.setImageDrawable(issuerModel.getLogo());
                } else {
                    mLogoImage.setImageResource(R.drawable.ic_placeholder_image);
                }
            }
        }

    }

}
