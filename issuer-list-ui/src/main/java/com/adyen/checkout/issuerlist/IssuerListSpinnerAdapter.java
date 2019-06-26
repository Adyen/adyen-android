/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/4/2019.
 */

package com.adyen.checkout.issuerlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.adyen.checkout.issuerlist.ui.R;

import java.util.List;

public class IssuerListSpinnerAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    private List<IssuerModel> mIssuerList;

    private boolean mHideIssuerLogo;

    IssuerListSpinnerAdapter(@NonNull Context context, @NonNull List<IssuerModel> issuerList) {
        this(context, issuerList, false);
    }

    IssuerListSpinnerAdapter(@NonNull Context context, @NonNull List<IssuerModel> issuerList, boolean hideIssuerLogo) {
        mInflater = LayoutInflater.from(context);
        mIssuerList = issuerList;
        mHideIssuerLogo = hideIssuerLogo;
    }

    void updateIssuers(@NonNull List<IssuerModel> issuerList) {
        mIssuerList = issuerList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mIssuerList.size();
    }

    @Override
    @NonNull
    public IssuerModel getItem(int position) {
        return mIssuerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        final View view;
        final IssuerModel issuer = getItem(position);

        // TODO: 15/03/2019 check if optimization with ViewHolder is possible
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.spinner_list_with_image, parent, false);
        }

        final AppCompatImageView issuerLogo = view.findViewById(R.id.imageView_logo);
        final AppCompatTextView issuerName = view.findViewById(R.id.textView_text);

        issuerName.setText(issuer.getName());
        if (!mHideIssuerLogo) {
            if (issuer.getLogo() != null) {
                issuerLogo.setImageDrawable(issuer.getLogo());
            } else {
                issuerLogo.setImageResource(R.drawable.ic_placeholder_image);
            }
        } else {
            issuerLogo.setVisibility(View.GONE);
        }

        return view;
    }
}
