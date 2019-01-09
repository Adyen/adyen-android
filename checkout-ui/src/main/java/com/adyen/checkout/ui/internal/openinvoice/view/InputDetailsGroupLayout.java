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
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.FieldSetConfiguration;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.Adapter;
import com.adyen.checkout.ui.internal.openinvoice.control.InputDetailController;
import com.adyen.checkout.ui.internal.openinvoice.control.SimpleEditTextValidator;
import com.adyen.checkout.ui.internal.openinvoice.control.ValidationChanger;
import com.adyen.checkout.ui.internal.openinvoice.control.ValidationCheckDelegate;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class InputDetailsGroupLayout extends LinearLayout implements ValidationChanger, ValidationChanger.ValidationChangeListener {

    @NonNull
    protected LinkedHashMap<String, InputDetailController> mControllerMap = new LinkedHashMap<>();

    private TextView mTitle;
    private TextView mReadOnlyDetails;
    private FieldSetConfiguration.FieldVisibility mFormVisibility = FieldSetConfiguration.FieldVisibility.EDITABLE;
    private HashSet<ValidationChangeListener> mListeners = new HashSet<>();
    private boolean mIsValid;

    public InputDetailsGroupLayout(@NonNull Context context) {
        super(context);
    }

    public InputDetailsGroupLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InputDetailsGroupLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = findViewById(R.id.textView_title);
        mReadOnlyDetails = findViewById(R.id.textView_readOnlyDetails);
    }

    /**
     * Sets the text for the title view of this group.
     * This can be used with the include tag to create multiple groups of the same type with different titles.
     *
     * @param titleResource The resource int of the string to be set.
     */
    public void setTitle(@StringRes int titleResource) {
        if (mTitle != null) {
            mTitle.setText(titleResource);
        }
    }

    @NonNull
    protected TextView getReadOnlyDetails() {
        return mReadOnlyDetails;
    }

    /**
     * Uses a base {@link InputDetail} that has a group of child InputDetails to populate the group view.
     *
     * @param inputDetailGroup The parent InputDetail with a sub-group of other InputDetails
     * @throws CheckoutException a number of exceptions can happen while parsing the data.
     */
    public void populateInputDetailGroup(@NonNull InputDetail inputDetailGroup) throws CheckoutException {
        setFormVisibility(inputDetailGroup);
    }

    protected abstract void mapChildInputDetails(@NonNull List<InputDetail> childDetails) throws CheckoutException;

    protected abstract void parseReadOnlyDetails(@Nullable List<InputDetail> childDetails) throws CheckoutException;

    @Override
    public boolean isValid() {
        if (getFormVisibility() != FieldSetConfiguration.FieldVisibility.EDITABLE) {
            return true;
        }

        for (InputDetailController controller : mControllerMap.values()) {
            if (!controller.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public synchronized void addValidationChangeListener(@NonNull ValidationChangeListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeValidationChangeListener(@NonNull ValidationChangeListener listener) {
        mListeners.remove(listener);
    }

    protected synchronized void notifyValidationListeners(boolean isValid) {
        for (ValidationChangeListener listener : mListeners) {
            listener.onValidationChanged(isValid);
        }
    }

    @Override
    public void onValidationChanged(boolean isValid) {
        boolean newValidity = isValid();
        if (mIsValid != newValidity) {
            mIsValid = newValidity;
            notifyValidationListeners(mIsValid);
        }
    }

    /**
     * @return The visibility type of this form.
     */
    @NonNull
    public FieldSetConfiguration.FieldVisibility getFormVisibility() {
        return mFormVisibility;
    }

    protected void setFormVisibility(@NonNull InputDetail inputDetailGroup) throws CheckoutException {

        FieldSetConfiguration.FieldVisibility layoutVisibility = null;

        try {
            FieldSetConfiguration config = inputDetailGroup.getConfiguration(FieldSetConfiguration.class);
            if (config != null) {
                layoutVisibility = config.getFieldVisibility();
            }
        } catch (CheckoutException e) {
            //configuration not present or malformed
        }

        if (layoutVisibility != null) {
            mFormVisibility = layoutVisibility;

            switch (mFormVisibility) {
                case HIDDEN:
                    setVisibility(GONE);
                    parseReadOnlyDetails(inputDetailGroup.getChildInputDetails());
                    break;
                case READ_ONLY:
                    mReadOnlyDetails.setVisibility(VISIBLE);
                    parseReadOnlyDetails(inputDetailGroup.getChildInputDetails());
                    break;
                case EDITABLE:
                    //default behavior is show as editable, intentional fallthrough
                default:
                    if (inputDetailGroup.getChildInputDetails() == null) {
                        throw new CheckoutException.Builder("InputDetail form has no child data", null).build();
                    }
                    mapChildInputDetails(inputDetailGroup.getChildInputDetails());
                    break;
            }
        }
    }

    protected void mapSimpleEditTextDetail(@NonNull EditText editText, @NonNull ViewGroup layout, @NonNull InputDetail childDetail) {
        final ValidationCheckDelegate detailValidator = new SimpleEditTextValidator(childDetail, editText);
        mapEditTextWithValidator(editText, layout, childDetail, detailValidator);
    }

    protected void mapEditTextWithValidator(@NonNull EditText editText, @NonNull ViewGroup layout, @NonNull InputDetail childDetail,
            @NonNull ValidationCheckDelegate detailValidator) {
        final InputDetailController controller = new InputDetailController(childDetail, editText, layout, detailValidator);
        controller.addValidationChangeListener(this);
        mControllerMap.put(childDetail.getKey(), controller);

        //populate existing value
        if (!TextUtils.isEmpty(childDetail.getValue())) {
            editText.setText(childDetail.getValue());
        }
    }

    protected static void populateSpinner(@NonNull final Spinner spinner, @NonNull InputDetail inputDetail) {
        //spinners don't come localized from the server
        Adapter<Item> adapter = Adapter.forSpinner(new Adapter.TextDelegate<Item>() {
            @NonNull
            @Override
            public String getText(@NonNull Item item) {
                return item.getName();
            }
        });
        adapter.setViewCustomizationDelegate(new Adapter.ViewCustomizationDelegate() {
            @Override
            public void customizeView(@NonNull CheckedTextView textView) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, spinner.getContext().getResources().getDimension(R.dimen.primarySpinnerTextSize));
            }
        });
        adapter.setItems(inputDetail.getItems());
        spinner.setAdapter(adapter);

        //set selected value
        if (!TextUtils.isEmpty(inputDetail.getValue())) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (inputDetail.getValue().equals(adapter.getItem(i).getId())) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    @Nullable
    protected static String getTextFromInput(@Nullable TextView editText) {
        String text = null;

        if (editText != null
                && editText.getVisibility() == VISIBLE
                && !TextUtils.isEmpty(editText.getText())) {
            text = editText.getText().toString();
        }

        return text;
    }

    @Nullable
    protected static String getIdFromItemSpinner(@Nullable Spinner spinner) {
        String id = null;

        if (spinner != null && spinner.getSelectedItem() != null && spinner.getVisibility() == VISIBLE) {
            Item item = (Item) spinner.getSelectedItem();
            if (!TextUtils.isEmpty(item.getId())) {
                id = item.getId();
            }
        }

        return id;
    }
}
