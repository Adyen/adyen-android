/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.FieldSetConfiguration;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.ui.internal.common.view.DatePickerWidget;
import com.adyen.checkout.ui.internal.openinvoice.view.InputErrorFeedbackLayout;

import java.util.HashSet;

public class InputDetailController implements ValidationChanger, ValidationChanger.ValidationChangeListener {
    private static final String TAG = InputDetailController.class.getSimpleName();

    private InputDetail mInputDetail;
    private View mInputView;
    private ViewGroup mInputContainer;

    private InputErrorFeedbackLayout mErrorFeedbackLayout;

    private boolean mIsValid;

    private ValidationCheckDelegate mValidationCheckDelegate;
    private VisibilityControlDelegate mVisibilityControlDelegate;
    private HashSet<ValidationChangeListener> mListeners = new HashSet<>();


    public InputDetailController(@NonNull InputDetail inputDetail, @NonNull View inputView, @Nullable ViewGroup inputContainer,
                                 @NonNull ValidationCheckDelegate validationCheckDelegate) {

        mInputDetail = inputDetail;
        mInputView = inputView;
        mInputContainer = inputContainer;

        mValidationCheckDelegate = validationCheckDelegate;
        mValidationCheckDelegate.addValidationChangeListener(this);
        mVisibilityControlDelegate = VisibilityControlFactory.getVisibilityDelegate(mInputView);

        mErrorFeedbackLayout = getErrorFeedbackLayout();

        setDetailViewVisibility();

        //show UI error if user changes focus
        mInputView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (mValidationCheckDelegate.getValidationState() == ValidationCheckDelegate.ValidationState.INCORRECT_FORMAT) {
                        setErrorStateUiFeedback(InputErrorFeedbackLayout.ErrorFeedbackState.INCORRECT_FORMAT);
                    }
                }
            }
        });
    }

    @Nullable
    private InputErrorFeedbackLayout getErrorFeedbackLayout() {
        for (int i = 0; i < mInputContainer.getChildCount(); i++) {
            if (mInputContainer.getChildAt(i) instanceof InputErrorFeedbackLayout) {
                return (InputErrorFeedbackLayout) mInputContainer.getChildAt(i);
            }
        }
        return null;
    }

    private void setDetailViewVisibility() {
        if (mInputDetail.isOptional()) {
            mInputContainer.setVisibility(GONE);
            return;
        }

        mInputContainer.setVisibility(VISIBLE);

        switch (getConfigVisibility()) {
            case READ_ONLY:
                mVisibilityControlDelegate.setReadOnlyInputView();
                break;
            case HIDDEN:
                mInputContainer.setVisibility(GONE);
                break;
            case EDITABLE:
                mVisibilityControlDelegate.setEditableInputView();
                break;
            default:
                Log.e(TAG, "Unexpected field visibility - " + getConfigVisibility());
                mVisibilityControlDelegate.setEditableInputView();
                break;
        }
    }

    @NonNull
    private FieldSetConfiguration.FieldVisibility getConfigVisibility() {
        try {
            FieldSetConfiguration config = mInputDetail.getConfiguration(FieldSetConfiguration.class);
            if (config != null) {
                return config.getFieldVisibility();
            }
        } catch (CheckoutException e) {
            //configuration might not exist
        }
        return FieldSetConfiguration.FieldVisibility.EDITABLE;
    }

    @Override
    public boolean isValid() {
        return mValidationCheckDelegate.isValid();
    }

    @Override
    public synchronized void addValidationChangeListener(@NonNull ValidationChangeListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeValidationChangeListener(@NonNull ValidationChangeListener listener) {
        mListeners.remove(listener);
    }

    private synchronized void notifyValidationListeners(boolean isValid) {
        for (ValidationChangeListener listener : mListeners) {
            listener.onValidationChanged(isValid);
        }
    }

    @Override
    public void onValidationChanged(boolean isValid) {
        if (mIsValid != isValid) {
            mIsValid = isValid;
            notifyValidationListeners(mIsValid);
        }
        //Reset UI if validation changes
        if (mValidationCheckDelegate.getValidationState() != ValidationCheckDelegate.ValidationState.INCORRECT_FORMAT) {
            setErrorStateUiFeedback(InputErrorFeedbackLayout.ErrorFeedbackState.NONE);
        }
    }

    private void setErrorStateUiFeedback(InputErrorFeedbackLayout.ErrorFeedbackState errorFeedbackState) {
        if (getConfigVisibility() != FieldSetConfiguration.FieldVisibility.EDITABLE) {
            return;
        }

        //currently only showing feedback for invalid format error
        if (errorFeedbackState ==  InputErrorFeedbackLayout.ErrorFeedbackState.INCORRECT_FORMAT) {
            mVisibilityControlDelegate.setErrorFeedbackView();
        } else {
            mVisibilityControlDelegate.setEditableInputView();
        }

        if (mErrorFeedbackLayout != null && (errorFeedbackState == InputErrorFeedbackLayout.ErrorFeedbackState.NONE
                    || errorFeedbackState == InputErrorFeedbackLayout.ErrorFeedbackState.INCORRECT_FORMAT)) {
            mErrorFeedbackLayout.setErrorState(errorFeedbackState);
        }
    }

    private static class VisibilityControlFactory {

        static VisibilityControlDelegate getVisibilityDelegate(View view) {
            if (view instanceof EditText) {
                return new EditTextVisualizationControl((EditText) view);
            }
            if (view instanceof Spinner) {
                return new GenericVisualizationControl(view);
            }
            if (view instanceof DatePickerWidget) {
                return new GenericVisualizationControl(view);
            }

            return new GenericVisualizationControl(view);
        }
    }
}
