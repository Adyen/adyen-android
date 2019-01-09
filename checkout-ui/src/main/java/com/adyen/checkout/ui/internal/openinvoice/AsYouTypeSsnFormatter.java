/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Formatter for Swedish Social Security Number input of format "YY MM DD NNNN".
 */
public class AsYouTypeSsnFormatter implements TextWatcher {

    public static final int MAX_SIZE = 13;
    @NonNull
    public static final String SEPARATOR = " ";

    private static final Set<Integer> SPACING_INDEXES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(2, 5, 8)));

    private boolean mIsTransforming;
    private int mDeletePos;

    private SsnInputCompleteCallback mCallback;

    public AsYouTypeSsnFormatter(@Nullable SsnInputCompleteCallback callback) {
        mCallback = callback;
    }

    @Override
    public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        if (!mIsTransforming) {
            mDeletePos = -1;
            //if deleting 1 character next to a separator, also remove the separator
            if (count == 0 && before == 1 && isSpacingIndex(start - 1)) {
                mDeletePos = start;
            }
        }
    }

    @Override
    public void afterTextChanged(@NonNull Editable s) {
        if (!mIsTransforming) {

            String input = s.toString();
            StringBuilder stringBuilder = new StringBuilder(input);

            if (mDeletePos > 0) {
                stringBuilder.replace(mDeletePos - 1, mDeletePos, "");
            }

            for (int i = 0; i < stringBuilder.length(); i++) {
                boolean isSeparator = SEPARATOR.equals(stringBuilder.substring(i, i + 1));

                if (isSeparator && !isSpacingIndex(i)) {
                    stringBuilder.deleteCharAt(i);
                } else if (isSpacingIndex(i) && !isSeparator) {
                    stringBuilder.insert(i, SEPARATOR);
                }
            }

            if (stringBuilder.length() > MAX_SIZE) {
                stringBuilder.delete(MAX_SIZE, stringBuilder.length());
            }

            if (!stringBuilder.toString().equals(s.toString())) {
                mIsTransforming = true;
                s.replace(0, s.length(), stringBuilder.toString());
            }

            if (s.length() == MAX_SIZE) {
                notifyListener(s.toString());
            }

            mIsTransforming = false;
        }
    }

    private boolean isSpacingIndex(int index) {
        return SPACING_INDEXES.contains(index);
    }

    private void notifyListener(@NonNull String ssnNumber) {
        if (mCallback != null) {
            mCallback.onSsnInputFinished(ssnNumber.replace(SEPARATOR, ""));
        }
    }

    public interface SsnInputCompleteCallback {
        void onSsnInputFinished(@NonNull String ssnNumber);
    }

}
