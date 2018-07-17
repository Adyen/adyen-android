package com.adyen.checkout.ui.internal.sepadirectdebit;

import android.support.annotation.NonNull;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/08/2017.
 */
public class NetherlandsAdapter extends CountryAdapter {
    private static final String[] BANK_CODES = new String[] {"ABNA", "INGB", "RABO", "SNSB", "TRIO", "ASNB", "BUNQ", "FRBK", "FVLB", "RBRB" };

    @NonNull
    @Override
    protected List<Suggestion> createSuggestions(@NonNull EditText editText, @NonNull String normalizedIban) {
        List<Suggestion> suggestions = new ArrayList<>();

        if (isLastIndexSelected(editText) && normalizedIban.length() >= 4) {
            String currentBankCode = normalizedIban.substring(4);

            if (currentBankCode.length() < 4) {
                for (String bankCode : BANK_CODES) {
                    if (bankCode.startsWith(currentBankCode)) {
                        String value = bankCode.replaceFirst(currentBankCode, "");
                        suggestions.add(new Suggestion(bankCode, value, editText.getText().length()));
                    }

                    if (suggestions.size() > 4) {
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
