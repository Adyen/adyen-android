/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.Address;
import com.adyen.checkout.core.model.FieldSetConfiguration;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.view.CustomTextInputLayout;
import com.adyen.checkout.ui.internal.openinvoice.control.InputDetailController;
import com.adyen.checkout.ui.internal.openinvoice.control.SimpleItemSpinnerValidator;
import com.adyen.checkout.ui.internal.openinvoice.control.ValidationCheckDelegateBase;

import java.util.List;

public class AddressDetailsInputLayout extends InputDetailsGroupLayout {

    private CustomTextInputLayout mStreetLayout;
    private CustomTextInputLayout mHouseNumberLayout;
    private CustomTextInputLayout mCityLayout;
    private CustomTextInputLayout mPostalCodeLayout;
    private LinearLayout mCountryLayout;

    private EditText mStreet;
    private EditText mHouseNumber;
    private EditText mCity;
    private EditText mPostalCode;
    private Spinner mCountry;

    private Address mReadOnlyAddress;

    public AddressDetailsInputLayout(@NonNull Context context) {
        super(context);
    }

    public AddressDetailsInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AddressDetailsInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            findViews();
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();

        SavedState savedState = new SavedState(parcelable);

        if (!TextUtils.isEmpty(mStreet.getText())) {
            savedState.street = mStreet.getText().toString();
        }
        if (!TextUtils.isEmpty(mHouseNumber.getText())) {
            savedState.houseNumber = mHouseNumber.getText().toString();
        }
        if (!TextUtils.isEmpty(mCity.getText())) {
            savedState.city = mCity.getText().toString();
        }
        if (!TextUtils.isEmpty(mPostalCode.getText())) {
            savedState.postalCode = mPostalCode.getText().toString();
        }
        String country = getIdFromItemSpinner(mCountry);
        if (!TextUtils.isEmpty(getIdFromItemSpinner(mCountry))) {
            savedState.country = country;
        }

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mStreet.setText(savedState.street);
        mHouseNumber.setText(savedState.houseNumber);
        mCity.setText(savedState.city);
        mPostalCode.setText(savedState.postalCode);

        if (!TextUtils.isEmpty(savedState.country)) {
            Adapter adapter = mCountry.getAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (savedState.country.equals(((Item) adapter.getItem(i)).getId())) {
                        mCountry.setSelection(i);
                        break;
                    }
                }
            }

        }
    }

    private void findViews() {
        mStreetLayout = findViewById(R.id.layout_street);
        mHouseNumberLayout = findViewById(R.id.layout_houseNumber);
        mCityLayout = findViewById(R.id.layout_city);
        mPostalCodeLayout = findViewById(R.id.layout_postalCode);
        mCountryLayout = findViewById(R.id.layout_country);

        mStreet = findViewById(R.id.editText_street);
        mHouseNumber = findViewById(R.id.editText_houseNumber);
        mCity = findViewById(R.id.editText_city);
        mPostalCode = findViewById(R.id.editText_postalCode);
        mCountry = findViewById(R.id.spinner_country);
    }

    @Override
    protected void mapChildInputDetails(@NonNull List<InputDetail> childDetails) throws CheckoutException {

        for (InputDetail childDetail : childDetails) {
            switch (childDetail.getKey()) {
                case Address.KEY_STREET:
                    mapSimpleEditTextDetail(mStreet, mStreetLayout, childDetail);
                    break;
                case Address.KEY_HOUSE_NUMBER_OR_NAME:
                    mapSimpleEditTextDetail(mHouseNumber, mHouseNumberLayout, childDetail);
                    break;
                case Address.KEY_CITY:
                    mapSimpleEditTextDetail(mCity, mCityLayout, childDetail);
                    break;
                case Address.KEY_POSTAL_CODE:
                    mapSimpleEditTextDetail(mPostalCode, mPostalCodeLayout, childDetail);
                    break;
                case Address.KEY_COUNTRY:
                    populateSpinner(mCountry, childDetail);

                    ValidationCheckDelegateBase detailValidator = new SimpleItemSpinnerValidator(childDetail, mCountry);
                    InputDetailController controller = new InputDetailController(childDetail, mCountry, mCountryLayout, detailValidator);
                    controller.addValidationChangeListener(this);
                    mControllerMap.put(childDetail.getKey(), controller);
                    break;
                case Address.KEY_STATE_OR_PROVINCE:
                    // Since State/province is always optional we are not displaying it for now
                    // Intentional fallthrough
                default:
                    if (!childDetail.isOptional()) {
                        throw new CheckoutException.Builder("Required detail does not have matching UI element", null).build();
                    }
                    break;
            }
        }
    }

    @Override
    protected void parseReadOnlyDetails(@Nullable List<InputDetail> childDetails) throws CheckoutException {
        if (childDetails == null) {
            if (getFormVisibility() == FieldSetConfiguration.FieldVisibility.READ_ONLY) {
                throw new CheckoutException.Builder("Read only InputDetail has no child data", null).build();
            } else {
                return;
            }
        }

        String street = "";
        String houseNumber = "";
        String city = "";
        String country = "";
        String postalCode = "";
        String stateOrProvince = "";

        String countryName = "";

        //get values from InputDetails
        for (InputDetail childDetail : childDetails) {
            switch (childDetail.getKey()) {
                case Address.KEY_STREET:
                    if (childDetail.getValue() != null) {
                        street = childDetail.getValue();
                    }
                    break;
                case Address.KEY_HOUSE_NUMBER_OR_NAME:
                    if (childDetail.getValue() != null) {
                        houseNumber = childDetail.getValue();
                    }
                    break;
                case Address.KEY_CITY:
                    if (childDetail.getValue() != null) {
                        city = childDetail.getValue();
                    }
                    break;
                case Address.KEY_COUNTRY:
                    if (childDetail.getValue() != null) {
                        country = childDetail.getValue();

                        if (childDetail.getItems() != null) {
                            for (Item item : childDetail.getItems()) {
                                if (item.getId().equals(childDetail.getValue())) {
                                    countryName = item.getName();
                                }
                            }
                        } else {
                            countryName = childDetail.getValue();
                        }
                    }
                    break;
                case Address.KEY_POSTAL_CODE:
                    if (childDetail.getValue() != null) {
                        postalCode = childDetail.getValue();
                    }
                    break;
                case Address.KEY_STATE_OR_PROVINCE:
                    // Since State/province is always optional we are not displaying it for now
                    break;
                default:
                    //unexpected value
                    break;
            }
        }

        Address.Builder addressBuilder = new Address.Builder(street, houseNumber, city, country, postalCode);
        if (!stateOrProvince.isEmpty()) {
            addressBuilder.setStateOrProvince(stateOrProvince);
        }

        mReadOnlyAddress = addressBuilder.build();

        //format result for UI
        if (getFormVisibility() == FieldSetConfiguration.FieldVisibility.READ_ONLY) {
            StringBuilder formattedResult = new StringBuilder();

            addFormatString(formattedResult, street, false);
            addFormatString(formattedResult, houseNumber, true);
            addFormatString(formattedResult, "\n", false);
            addFormatString(formattedResult, postalCode, false);
            addFormatString(formattedResult, city, true);
            addFormatString(formattedResult, stateOrProvince, true);
            addFormatString(formattedResult, "\n", false);
            addFormatString(formattedResult, countryName, false);

            getReadOnlyDetails().setText(formattedResult.toString());
        }
    }

    private void addFormatString(@NonNull StringBuilder stringBuilder, @Nullable String detailValue, boolean addTailingSpace) {
        if (!detailValue.isEmpty()) {
            if (addTailingSpace) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(detailValue);
        }
    }

    /**
     * Get the filled {@link Address} object with the data input from the user.
     * @return The filled {@link Address} object, returns null if {@link #isValid()}} is false.
     */
    @Nullable
    public Address getAddress() {
        if (!isValid()) {
            return null;
        }

        if (getFormVisibility() != FieldSetConfiguration.FieldVisibility.EDITABLE) {
            return mReadOnlyAddress;
        }

        String street = getTextFromInput(mStreet);
        String houseNumber = getTextFromInput(mHouseNumber);
        String city = getTextFromInput(mCity);
        String country = getIdFromItemSpinner(mCountry);
        String postalCode = getTextFromInput(mPostalCode);

        if (street == null || houseNumber == null || city == null || country == null || postalCode == null) {
            return null;
        }

        Address.Builder addressBuilder = new Address.Builder(
                street,
                houseNumber,
                city,
                country,
                postalCode
        );

        //State or province is not currently being used

        return addressBuilder.build();
    }

    /**
     * Fill in the response from the SSN Lookup.
     * @param address The address to add to the fields
     */
    public void fillSsnResponseAddress(@NonNull Address address) {
        if (getFormVisibility() != FieldSetConfiguration.FieldVisibility.EDITABLE) {
            return;
        }

        mStreet.setText(address.getStreet());
        mHouseNumber.setText(address.getHouseNumberOrName());
        mCity.setText(address.getCity());
        mPostalCode.setText(address.getPostalCode());

        if (!TextUtils.isEmpty(address.getCountry())) {
            Adapter adapter = mCountry.getAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (address.getCountry().equals(((Item) adapter.getItem(i)).getId())) {
                        mCountry.setSelection(i);
                        break;
                    }
                }
            }

        }
    }

    @SuppressWarnings("checkstyle:MemberName")
    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        String street;
        String houseNumber;
        String city;
        String postalCode;
        String country;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel source) {
            super(source);
            street = source.readString();
            houseNumber = source.readString();
            city = source.readString();
            postalCode = source.readString();
            country = source.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(street);
            out.writeString(houseNumber);
            out.writeString(city);
            out.writeString(postalCode);
            out.writeString(country);
        }
    }

}
