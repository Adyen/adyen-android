/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */

package com.adyen.checkout.sepa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.base.validation.ValidatedField;

class SepaOutputData implements OutputData {

    private final ValidatedField<String> mOwnerNameField;
    private final ValidatedField<String> mIbanNumberField;
    private final Iban mIban;

    SepaOutputData(@NonNull String ownerName, @NonNull String ibanNumber) {
        mOwnerNameField = new ValidatedField<>(ownerName,
                TextUtils.isEmpty(ownerName) ? ValidatedField.Validation.PARTIAL : ValidatedField.Validation.VALID);
        mIban = Iban.parse(ibanNumber);
        mIbanNumberField = validateIbanNumber(ibanNumber, mIban);
    }

    @Override
    public boolean isValid() {
        return mOwnerNameField.isValid() && mIbanNumberField.isValid();
    }

    @NonNull
    public ValidatedField<String> getOwnerNameField() {
        return mOwnerNameField;
    }

    @NonNull
    public ValidatedField<String> getIbanNumberField() {
        return mIbanNumberField;
    }

    @Nullable
    public Iban getIban() {
        return mIban;
    }

    private ValidatedField<String> validateIbanNumber(@NonNull String ibanNumber, @Nullable Iban iban) {
        final ValidatedField.Validation validation;
        if (iban != null) {
            validation = ValidatedField.Validation.VALID;
        } else if (Iban.isPartial(ibanNumber)) {
            validation = ValidatedField.Validation.PARTIAL;
        } else {
            validation = ValidatedField.Validation.INVALID;
        }

        return new ValidatedField<>(ibanNumber, validation);
    }
}
