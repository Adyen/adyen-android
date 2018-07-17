package com.adyen.checkout.ui.internal.common.util.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 15/08/2017.
 */
public class SimpleDiffCallback<T extends SimpleDiffCallback.Comparable<T>> extends DiffUtil.Callback {
    private List<T> mOldItems;

    private List<T> mNewItems;

    public SimpleDiffCallback(@NonNull List<T> oldItems, @NonNull List<T> newItems) {
        mOldItems = oldItems;
        mNewItems = newItems;
    }

    @Override
    public int getOldListSize() {
        return mOldItems.size();
    }

    @Override
    public int getNewListSize() {
        return mNewItems.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldItems.get(oldItemPosition).isSameItem(mNewItems.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldItems.get(oldItemPosition).isSameContent(mNewItems.get(newItemPosition));
    }

    public interface Comparable<T> {
        boolean isSameItem(@NonNull T newItem);

        boolean isSameContent(@NonNull T newItem);
    }
}
