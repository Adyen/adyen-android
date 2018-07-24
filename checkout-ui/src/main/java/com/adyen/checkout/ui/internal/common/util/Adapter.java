package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.adyen.checkout.ui.R;

import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 20/07/2018.
 */
public final class Adapter<T> extends BaseAdapter implements Filterable {
    private final boolean mUsePaddingInGetView;

    private final TextDelegate<T> mTextDelegate;

    private List<T> mItems;

    @NonNull
    public static <T> Adapter<T> forSpinner(@NonNull TextDelegate<T> textDelegate) {
        return new Adapter<>(false, textDelegate);
    }

    @NonNull
    public static <T> Adapter<T> forAutoCompleteTextView(@NonNull TextDelegate<T> textDelegate) {
        return new Adapter<>(true, textDelegate);
    }

    private Adapter(boolean usePaddingInGetView, @NonNull TextDelegate<T> textDelegate) {
        mUsePaddingInGetView = usePaddingInGetView;
        mTextDelegate = textDelegate;
    }

    @Override
    public int getCount() {
        return mItems != null ? mItems.size() : 0;
    }

    @NonNull
    @Override
    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getViewInternal(position, convertView, parent, mUsePaddingInGetView);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getViewInternal(position, convertView, parent, true);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public CharSequence convertResultToString(@NonNull Object resultValue) {
                //noinspection unchecked
                return mTextDelegate.getText((T) resultValue);
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<T> giroPayIssuers = mItems;

                FilterResults filterResults = new FilterResults();
                filterResults.count = giroPayIssuers != null ? giroPayIssuers.size() : 0;
                filterResults.values = giroPayIssuers;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }

    public void setItems(@Nullable List<T> items) {
        if ((mItems == null || mItems.isEmpty()) && (items == null || items.isEmpty())) {
            return;
        }

        if (mItems != null && mItems.equals(items)) {
            return;
        }

        mItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    private View getViewInternal(int position, @Nullable View convertView, @NonNull ViewGroup parent, boolean padView) {
        View view;

        if (convertView == null) {
            view = createViewWithPadding(parent, padView);
        } else {
            view = convertView;
        }

        T item = getItem(position);
        bindView(item, view);

        return view;
    }

    @NonNull
    private View createViewWithPadding(@NonNull ViewGroup parent, boolean withPadding) {
        Context context = parent.getContext();

        int padding = withPadding ? context.getResources().getDimensionPixelSize(R.dimen.standard_margin) : 0;

        View view = LayoutInflater.from(context).inflate(R.layout.item_dropdown, parent, false);
        view.setPadding(padding, padding, padding, padding);

        return view;
    }

    private void bindView(@NonNull T item, @NonNull View view) {
        String text = mTextDelegate.getText(item);
        ((TextView) view).setText(text);
    }

    public interface TextDelegate<T> {
        @NonNull
        String getText(@NonNull T input);
    }
}
