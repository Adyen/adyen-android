/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

import android.support.annotation.NonNull;

/**
 * An item or group that can be validated for the the inputs it contains.
 * This object can also notify listeners when the status of the validation has changed.
 */
public interface ValidationChanger {

    /**
     * Adds a {@link ValidationChangeListener} to be notified when the valid state of this form has changed.
     *
     * @param listener The listener.
     */
    void addValidationChangeListener(@NonNull ValidationChangeListener listener);

    /**
     * Removes a {@link ValidationChangeListener}.
     *
     * @param listener The listener to be removed.
     */
    void removeValidationChangeListener(@NonNull ValidationChangeListener listener);

    /**
     * Checks if the details input from the user are valid and ready to process the payment.
     *
     * @return True if inputs are valid, False otherwise.
     */
    boolean isValid();

    /**
     * Listener to receive updates on the validity of the user input details.
     */
    interface ValidationChangeListener {

        /**
         * The combined validation of the input details from this group has changed.
         * @param isValid If the current set of inputs is valid or not.
         */
        void onValidationChanged(boolean isValid);
    }
}
