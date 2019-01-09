/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

import android.support.annotation.NonNull;

public abstract class ValidationCheckDelegateBase implements ValidationCheckDelegate {

    private ValidationChangeListener mListener;
    private ValidationState mCurrentValidationState;

    @Override
    public boolean isValid() {
        ValidationState validationState = getValidationState();
        boolean isValid = validationState == ValidationState.VALID;

        if (mCurrentValidationState != validationState) {
            mCurrentValidationState = validationState;
            mListener.onValidationChanged(isValid);
        }

        return isValid;
    }

    @NonNull
    @Override
    public abstract ValidationState getValidationState();

    @Override
    public void addValidationChangeListener(@NonNull ValidationChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void removeValidationChangeListener(@NonNull ValidationChangeListener listener) {
        mListener = null;
    }
}
