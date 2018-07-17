package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adyen.checkout.ui.R;

import java.util.List;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 25/08/2017.
 */
public abstract class SimpleArrayAdapter<T> extends ArrayAdapter<T> {
    private final int mStandardPadding;

    public SimpleArrayAdapter(@NonNull Context context) {
        super(context, R.layout.item_dropdown);

        mStandardPadding = context.getResources().getDimensionPixelSize(R.dimen.standard_margin);
    }
    public SimpleArrayAdapter(@NonNull Context context, @NonNull List<T> objects) {
        super(context, R.layout.item_dropdown, objects);

        mStandardPadding = context.getResources().getDimensionPixelSize(R.dimen.standard_margin);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        view.setPadding(0, 0, 0, 0);
        setText(position, view);

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        view.setPadding(mStandardPadding, mStandardPadding, mStandardPadding, mStandardPadding);
        setText(position, view);

        return view;
    }

    @NonNull
    protected abstract String getText(@NonNull T item);

    private void setText(int position, @NonNull View view) {
        T item = getItem(position);

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setText(item != null ? getText(item) : "");
        }
    }
}
