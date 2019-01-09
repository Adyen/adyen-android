/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 16/08/2017.
 */

package com.adyen.checkout.ui.internal.sepadirectdebit;

import android.support.annotation.NonNull;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class NetherlandsAdapter extends CountryAdapter {
    private static final String[] BANK_CODES = new String[] {"ABNA", "INGB", "RABO", "SNSB", "TRIO", "ASNB", "BUNQ", "FRBK", "FVLB", "RBRB" };

    private static final int MAX_SUGGESTIONS = 4;

    @NonNull
    @Override
    protected List<Suggestion> createSuggestions(@NonNull EditText editText, @NonNull String normalizedIban) {
        List<Suggestion> suggestions = new ArrayList<>();

        if (isLastIndexSelected(editText) && normalizedIban.length() >= COUNTRY_BLOCK_LENGTH) {
            String currentBankCode = normalizedIban.substring(COUNTRY_BLOCK_LENGTH);

            if (currentBankCode.length() < COUNTRY_BLOCK_LENGTH) {
                for (String bankCode : BANK_CODES) {
                    if (bankCode.startsWith(currentBankCode)) {
                        String value = bankCode.replaceFirst(currentBankCode, "");
                        suggestions.add(new Suggestion(bankCode, value, editText.getText().length()));
                    }

                    if (suggestions.size() > MAX_SUGGESTIONS) {
                        break;
                    }
                }
            }
        }

        return suggestions;
    }

    private boolean isLastIndexSelected(@NonNull EditText editText) {
        int length = editText.length();
        return editText.getSelectionStart() == length && editText.getSelectionEnd() == length;
    }
}
