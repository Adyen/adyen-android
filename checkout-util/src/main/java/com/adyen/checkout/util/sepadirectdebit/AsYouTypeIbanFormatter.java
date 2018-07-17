package com.adyen.checkout.util.sepadirectdebit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.adyen.checkout.util.internal.SimpleTextWatcher;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/10/2017.
 */
public final class AsYouTypeIbanFormatter extends SimpleTextWatcher {
    private static final char SPACING_CHAR = ' ';

    private static final String SPACING_STRING = String.valueOf(SPACING_CHAR);

    private final EditText mEditText;

    private boolean mDeleted;

    private boolean mTransforming;

    @NonNull
    public static TextWatcher attach(@NonNull EditText editText) {
        AsYouTypeIbanFormatter formatter = new AsYouTypeIbanFormatter(editText);
        editText.addTextChangedListener(formatter);

        return formatter;
    }

    private AsYouTypeIbanFormatter(@NonNull EditText editText) {
        mEditText = editText;
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
                    if (c != SPACING_CHAR) {
                        if (!isValidCharacterClass(c)) {
                            s.replace(i, i + 1, SPACING_STRING);
                        } else {
                            s.insert(i, SPACING_STRING);
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
                    if (!isValidCharacterClass(c)) {
                        s.delete(i, i + 1);
                        length = s.length();
                    } else {
                        Character transformed = transform(c);

                        if (transformed != null) {
                            s.replace(i, i + 1, String.valueOf(transformed));
                        }
                    }
                }
            }

            mTransforming = false;
        }
    }

    private boolean isSpacingIndex(int index) {
        return (index + 1) % 5 == 0;
    }

    private boolean isValidCharacterClass(char c) {
        return Character.isLetterOrDigit(c);
    }

    @Nullable
    private Character transform(char c) {
        return Character.isLowerCase(c) ? Character.toUpperCase(c) : null;
    }
}
