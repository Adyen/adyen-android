/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

import android.view.View;

class GenericVisualizationControl implements VisibilityControlDelegate {

    private View mView;

    GenericVisualizationControl(View view) {
        mView = view;
    }

    @Override
    public void setEditableInputView() {
        mView.setEnabled(true);
    }

    @Override
    public void setReadOnlyInputView() {
        mView.setEnabled(false);
    }

    @Override
    public void setErrorFeedbackView() {
        //noop
    }
}
