/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adyen.checkout.ui.R;


public class InputErrorFeedbackLayout extends LinearLayout {

    private TextView mErrorText;
    private ImageView mErrorImage;

    public InputErrorFeedbackLayout(@NonNull Context context) {
        super(context);
    }

    public InputErrorFeedbackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InputErrorFeedbackLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mErrorText = findViewById(R.id.textView_error_message);
        mErrorImage = findViewById(R.id.imageView_error_icon);
    }

    public void setErrorState(@NonNull ErrorFeedbackState errorFeedbackState) {
        setErrorState(errorFeedbackState, null);
    }

    /**
     * Set the error state that will be displayed.
     * You can use a custom state if you also pass a {@link CustomInputErrorDelegate} to treat the UI feedback.
     *
     * @param errorFeedbackState The ErrorFeedbackState that will be set.
     * @param errorDelegate The delegate that will set the UI state of the custom error state.
     */
    public void setErrorState(@NonNull ErrorFeedbackState errorFeedbackState, @Nullable CustomInputErrorDelegate errorDelegate) {
        switch (errorFeedbackState) {
            case NONE:
                setVisibility(GONE);
                break;
            case FIELD_EMPTY:
                setVisibility(VISIBLE);
                mErrorText.setText(R.string.checkout_input_error_empty_field);
                break;
            case INCORRECT_FORMAT:
                setVisibility(VISIBLE);
                mErrorText.setText(R.string.checkout_input_error_incorrect_format);
                break;
            case CUSTOM_ERROR:
                if (errorDelegate != null) {
                    setVisibility(VISIBLE);
                    errorDelegate.setupText(mErrorText);
                    errorDelegate.setupIcon(mErrorImage);
                }
                break;
            default:
                setVisibility(GONE);
                break;
        }
    }

    /**
     * Enum that indicates the visual feedback type for an input error.
     */
    public enum ErrorFeedbackState {
        /** There is no error, hide error UI. */
        NONE,
        /** Field is empty and needs to be filled in. */
        FIELD_EMPTY,
        /** The input format is incorrect. */
        INCORRECT_FORMAT,
        /**
         * A custom error.
         * Provided by a {@link CustomInputErrorDelegate}.
         */
        CUSTOM_ERROR
    }

    /**
     * Delegate that can set the UI user feedback for a custom input error.
     */
    public interface CustomInputErrorDelegate {
        /**
         * Setup how the error text should be displayed.
         * @param errorText The error TextView
         */
        void setupText(@NonNull TextView errorText);

        /**
         * Setup how the error icon should be displayed.
         * @param errorIcon The ImageView of the error icon.
         */
        void setupIcon(@NonNull ImageView errorIcon);
    }
}
