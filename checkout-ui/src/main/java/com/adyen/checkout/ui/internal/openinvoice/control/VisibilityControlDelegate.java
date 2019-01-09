/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

/**
 * Interface for delegate class that will customize a View according to state it should represent.
 */
public interface VisibilityControlDelegate {

    /**
     * Sets the View as an Editable field.
     */
    void setEditableInputView();

    /**
     * Sets the View as a Read Only field.
     */
    void setReadOnlyInputView();

    /**
     * Sets the View to represent an input Error feedback.
     */
    void setErrorFeedbackView();
}
