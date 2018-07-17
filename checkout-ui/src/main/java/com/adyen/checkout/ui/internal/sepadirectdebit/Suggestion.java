package com.adyen.checkout.ui.internal.sepadirectdebit;

import android.support.annotation.NonNull;

import com.adyen.checkout.ui.internal.common.util.recyclerview.SimpleDiffCallback;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/08/2017.
 */
public class Suggestion implements SimpleDiffCallback.Comparable<Suggestion> {
    private String mName;

    private String mValue;

    private int mTargetIndex;

    Suggestion(@NonNull String name, @NonNull String value, int targetIndex) {
        mName = name;
        mValue = value;
        mTargetIndex = targetIndex;
    }

    @Override
    public boolean isSameItem(@NonNull Suggestion newItem) {
        return mName.equals(newItem.mName);
    }

    @Override
    public boolean isSameContent(@NonNull Suggestion newItem) {
        return true;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @NonNull
    public String getValue() {
        return mValue;
    }

    public int getTargetIndex() {
        return mTargetIndex;
    }
}
