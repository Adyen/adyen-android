/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */

package com.adyen.checkout.sepa;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.components.base.OutputData;
import com.adyen.checkout.components.ui.FieldState;
import com.adyen.checkout.components.ui.Validation;

class SepaOutputData implements OutputData {

    private final FieldState<String> mOwnerNameField;
    private final FieldState<String> mIbanNumberField;
    private final Iban mIban;

    SepaOutputData(@NonNull String ownerName, @NonNull String ibanNumber) {
        mOwnerNameField = new FieldState<>(ownerName,
                TextUtils.isEmpty(ownerName)
                        ? new Validation.Invalid(R.string.checkout_holder_name_not_valid)
                        : Validation.Valid.INSTANCE);
        mIban = Iban.parse(ibanNumber);
        mIbanNumberField = validateIbanNumber(ibanNumber, mIban);
    }

    @Override
    public boolean isValid() {
        return mOwnerNameField.getValidation().isValid() && mIbanNumberField.getValidation().isValid();
    }

    @NonNull
    public FieldState<String> getOwnerNameField() {
        return mOwnerNameField;
    }

    @NonNull
    public FieldState<String> getIbanNumberField() {
        return mIbanNumberField;
    }

    @Nullable
    public Iban getIban() {
        return mIban;
    }

    private FieldState<String> validateIbanNumber(@NonNull String ibanNumber, @Nullable Iban iban) {
        final Validation validation;
        if (iban != null) {
            validation = Validation.Valid.INSTANCE;
        } else {
            validation = new Validation.Invalid(R.string.checkout_iban_not_valid);
        }

        return new FieldState<>(ibanNumber, validation);
    }
}
