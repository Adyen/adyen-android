package com.adyen.checkout.core.card.internal;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.adyen.checkout.core.card.CardType;

/**
 * A {@link TextWatcher} that formats a card number to a readable format while typing.
 * <p>
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/10/2017.
 */
public final class AsYouTypeCardNumberFormatter implements TextWatcher {
    private final EditText mEditText;

    private final char mSeparatorChar;

    private final String mSeparatorString;

    private boolean mDeleted;

    private boolean mTransforming;

    /**
     * Attaches a {@link AsYouTypeCardNumberFormatter} to a given {@link EditText}.
     *
     * @param editText The {@link EditText} to attach the {@link AsYouTypeCardNumberFormatter} to.
     * @return The attached {@link TextWatcher}.
     */
    @NonNull
    static TextWatcher attach(@NonNull EditText editText, char separatorChar) {
        AsYouTypeCardNumberFormatter formatter = new AsYouTypeCardNumberFormatter(editText, separatorChar);
        editText.addTextChangedListener(formatter);

        return formatter;
    }

    private AsYouTypeCardNumberFormatter(@NonNull EditText editText, char separatorChar) {
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

            int length = s.length();

            for (int i = 0; i < length; i++) {
                char c = s.charAt(i);

                if (isSpacingIndex(i)) {
                    if (c != mSeparatorChar) {
                        if (!Character.isDigit(c)) {
                            s.replace(i, i + 1, mSeparatorString);
                        } else {
                            s.insert(i, mSeparatorString);
                            length = s.length();

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
                        length = s.length();
                    }
                }
            }

            mTransforming = false;
        }
    }

    private boolean isSpacingIndex(int index) {
        if (CardType.AMERICAN_EXPRESS.isEstimateFor(mEditText.getText().toString())) {
            return index == 4 || index == 11 || index == 17;
        } else {
            return (index + 1) % 5 == 0;
        }
    }
}
