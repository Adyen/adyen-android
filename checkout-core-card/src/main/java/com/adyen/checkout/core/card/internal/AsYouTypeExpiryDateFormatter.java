package com.adyen.checkout.core.card.internal;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * A {@link TextWatcher} that formats a card expiry date to a readable format while typing.
 * <p>
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/10/2017.
 */
public final class AsYouTypeExpiryDateFormatter implements TextWatcher {
    private final EditText mEditText;

    private final char mSeparatorChar;

    private final String mSeparatorString;

    private boolean mDeleted;

    private boolean mTransforming;

    /**
     * Attaches an {@link AsYouTypeExpiryDateFormatter} to a given {@link EditText}.
     *
     * @param editText The {@link EditText} to attach the {@link AsYouTypeExpiryDateFormatter} to.
     * @return The attached {@link TextWatcher}.
     */
    @NonNull
    static TextWatcher attach(@NonNull EditText editText, char separatorChar) {
        AsYouTypeExpiryDateFormatter formatter = new AsYouTypeExpiryDateFormatter(editText, separatorChar);
        editText.addTextChangedListener(formatter);

        return formatter;
    }

    private AsYouTypeExpiryDateFormatter(@NonNull EditText editText, char separatorChar) {
        mEditText = editText;
        mSeparatorChar = separatorChar;
        mSeparatorString = String.valueOf(mSeparatorChar);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Nothing to do.
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!mTransforming) {
            mDeleted = count == 0;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mTransforming) {
            mTransforming = true;

            if (s.length() == 1 && s.charAt(0) > '1') {
                s.insert(0, "0");
            }

            if (s.length() == 2 && !mDeleted) {
                if (s.toString().matches("\\d\\" + mSeparatorChar)) {
                    s.insert(0, "0");
                } else if (!s.toString().contains(mSeparatorString)) {
                    s.append(mSeparatorChar);
                }
            }

            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);

                if (i == 2) {
                    if (c != mSeparatorChar) {
                        if (!Character.isDigit(c)) {
                            s.replace(i, i + 1, mSeparatorString);
                        } else {
                            s.insert(i, mSeparatorString);

                            if (mDeleted) {
                                int selectionStart = mEditText.getSelectionStart();
                                int selectionEnd = mEditText.getSelectionEnd();
                                int newSelectionStart = selectionStart - 1 == i ? selectionStart - 1 : selectionStart;
                                int newSelectionEnd = selectionEnd - 1 == i ? selectionEnd - 1 : selectionEnd;
                                mEditText.setSelection(newSelectionStart, newSelectionEnd);
                            }
                        }
                    }
                } else {
                    if (!Character.isDigit(c)) {
                        s.delete(i, i + 1);
                    }
                }
            }

            mTransforming = false;
        }
    }
}
