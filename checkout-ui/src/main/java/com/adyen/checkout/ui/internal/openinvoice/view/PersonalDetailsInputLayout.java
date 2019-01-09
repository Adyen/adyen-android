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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.FieldSetConfiguration;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.core.model.Name;
import com.adyen.checkout.core.model.PersonalDetails;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.Adapter;
import com.adyen.checkout.ui.internal.common.view.CustomTextInputLayout;
import com.adyen.checkout.ui.internal.common.view.DatePickerWidget;
import com.adyen.checkout.ui.internal.openinvoice.control.DateValidator;
import com.adyen.checkout.ui.internal.openinvoice.control.EmailValidator;
import com.adyen.checkout.ui.internal.openinvoice.control.InputDetailController;
import com.adyen.checkout.ui.internal.openinvoice.control.SimpleItemSpinnerValidator;
import com.adyen.checkout.ui.internal.openinvoice.control.ValidationCheckDelegate;
import com.adyen.checkout.util.LocaleUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PersonalDetailsInputLayout extends InputDetailsGroupLayout {
    private static final String TAG = PersonalDetailsInputLayout.class.getSimpleName();

    private CustomTextInputLayout mFirstNameLayout;
    private CustomTextInputLayout mLastNameLayout;
    private LinearLayout mGenderLayout;
    private CustomTextInputLayout mDateOfBirthLayout;
    private CustomTextInputLayout mSocialSecurityNumberLayout;
    private CustomTextInputLayout mTelephoneNumberLayout;
    private CustomTextInputLayout mShopperEmailLayout;

    private EditText mFirstName;
    private EditText mLastName;
    private Spinner mGender;
    private DatePickerWidget mDateOfBirth;
    private EditText mSocialSecurityNumber;
    private EditText mTelephoneNumber;
    private EditText mShopperEmail;

    private PersonalDetails mReadOnlyPersonalDetails;

    public PersonalDetailsInputLayout(@NonNull Context context) {
        super(context);
    }

    public PersonalDetailsInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PersonalDetailsInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            findViews();
        }
    }

    private void findViews() {
        mFirstNameLayout = findViewById(R.id.layout_firstName);
        mLastNameLayout = findViewById(R.id.layout_lastName);
        mGenderLayout = findViewById(R.id.layout_gender);
        mDateOfBirthLayout = findViewById(R.id.layout_dateOfBirth);
        mSocialSecurityNumberLayout = findViewById(R.id.layout_socialSecurityNumber);
        mTelephoneNumberLayout = findViewById(R.id.layout_telephoneNumber);
        mShopperEmailLayout = findViewById(R.id.layout_shopperEmail);

        mFirstName = findViewById(R.id.editText_firstName);
        mLastName = findViewById(R.id.editText_lastName);
        mGender = findViewById(R.id.spinner_gender);
        mDateOfBirth = findViewById(R.id.datePicker_dateOfBirth);
        mSocialSecurityNumber = findViewById(R.id.editText_socialSecurityNumber);
        mTelephoneNumber = findViewById(R.id.editText_telephoneNumber);
        mShopperEmail = findViewById(R.id.editText_shopperEmail);
    }

    @Override
    protected void mapChildInputDetails(@NonNull List<InputDetail> childDetails) throws CheckoutException {

        InputDetailController controller;
        ValidationCheckDelegate detailValidator;

        for (InputDetail childDetail : childDetails) {
            switch (childDetail.getKey()) {
                case PersonalDetails.KEY_FIRST_NAME:
                    mapSimpleEditTextDetail(mFirstName, mFirstNameLayout, childDetail);
                    break;
                case PersonalDetails.KEY_LAST_NAME:
                    mapSimpleEditTextDetail(mLastName, mLastNameLayout, childDetail);
                    break;
                case PersonalDetails.KEY_GENDER:
                    populateGenderSpinner(childDetail);

                    detailValidator = new SimpleItemSpinnerValidator(childDetail, mGender);
                    controller = new InputDetailController(childDetail, mGender, mGenderLayout, detailValidator);
                    controller.addValidationChangeListener(this);
                    mControllerMap.put(childDetail.getKey(), controller);
                    break;
                case PersonalDetails.KEY_DATE_OF_BIRTH:
                    detailValidator = new DateValidator(childDetail, mDateOfBirth);
                    controller = new InputDetailController(childDetail, mDateOfBirth, mDateOfBirthLayout, detailValidator);
                    controller.addValidationChangeListener(this);
                    mControllerMap.put(childDetail.getKey(), controller);

                    if (!TextUtils.isEmpty(childDetail.getValue())) {
                        Date date = parseDate(childDetail.getValue());
                        if (date != null) {
                            mDateOfBirth.setDate(date);
                        }
                    }
                    break;
                case PersonalDetails.KEY_SOCIAL_SECURITY_NUMBER:
                    mapSimpleEditTextDetail(mSocialSecurityNumber, mSocialSecurityNumberLayout, childDetail);
                    break;
                case PersonalDetails.KEY_TELEPHONE_NUMBER:
                    mapSimpleEditTextDetail(mTelephoneNumber, mTelephoneNumberLayout, childDetail);
                    break;
                case PersonalDetails.KEY_SHOPPER_EMAIL:
                    EmailValidator emailValidator = new EmailValidator(childDetail, mShopperEmail);
                    mapEditTextWithValidator(mShopperEmail, mShopperEmailLayout, childDetail, emailValidator);
                    break;
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

        String firstName = "";
        String lastName = "";
        String gender = "";
        String dateOfBirthString = "";
        String socialSecurityNumber = "";
        String telephoneNumber = "";
        String shopperEmail = "";

        //get values from InputDetails
        for (InputDetail childDetail : childDetails) {
            switch (childDetail.getKey()) {
                case PersonalDetails.KEY_FIRST_NAME:
                    if (childDetail.getValue() != null) {
                        firstName = childDetail.getValue();
                    }
                    break;
                case PersonalDetails.KEY_LAST_NAME:
                    if (childDetail.getValue() != null) {
                        lastName = childDetail.getValue();
                    }
                    break;
                case PersonalDetails.KEY_GENDER:
                    if (childDetail.getValue() != null) {
                        gender = childDetail.getValue();
                    }
                    break;
                case PersonalDetails.KEY_DATE_OF_BIRTH:
                    if (childDetail.getValue() != null) {
                        dateOfBirthString = childDetail.getValue();
                    }
                    break;
                case PersonalDetails.KEY_SOCIAL_SECURITY_NUMBER:
                    if (childDetail.getValue() != null) {
                        socialSecurityNumber = childDetail.getValue();
                    }
                    break;
                case PersonalDetails.KEY_TELEPHONE_NUMBER:
                    if (childDetail.getValue() != null) {
                        telephoneNumber = childDetail.getValue();
                    }
                    break;
                case PersonalDetails.KEY_SHOPPER_EMAIL:
                    if (childDetail.getValue() != null) {
                        shopperEmail = childDetail.getValue();
                    }
                    break;
                default:
                    //unexpected value
                    break;
            }
        }

        PersonalDetails.Builder personalDetailsBuilder = new PersonalDetails.Builder(firstName, lastName, telephoneNumber, shopperEmail);


        Date dateOfBirth = parseDate(dateOfBirthString);

        if (dateOfBirth != null) {
            personalDetailsBuilder.setDateOfBirth(dateOfBirth);
        }
        if (!socialSecurityNumber.isEmpty()) {
            personalDetailsBuilder.setSocialSecurityNumber(socialSecurityNumber);
        }
        if (!gender.isEmpty()) {
            personalDetailsBuilder.setGender(gender);
        }

        mReadOnlyPersonalDetails = personalDetailsBuilder.build();

        //format result for UI
        if (getFormVisibility() == FieldSetConfiguration.FieldVisibility.READ_ONLY) {
            StringBuilder formattedResult = new StringBuilder();

            addDetailString(formattedResult, firstName, false);
            addDetailString(formattedResult, lastName, true);
            addDetailString(formattedResult, "\n", false);

            if (!gender.isEmpty()) {
                addDetailString(formattedResult, parseGenderString(gender), false);
                addDetailString(formattedResult, "\n", false);
            }
            if (dateOfBirth != null) {
                DateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY", LocaleUtil.getLocale(getContext()));
                addDetailString(formattedResult, dateFormat.format(dateOfBirth), false);
                addDetailString(formattedResult, "\n", false);
            }
            if (!socialSecurityNumber.isEmpty()) {
                addDetailString(formattedResult, socialSecurityNumber, false);
                addDetailString(formattedResult, "\n", false);
            }

            addDetailString(formattedResult, telephoneNumber, false);
            addDetailString(formattedResult, "\n", false);

            addDetailString(formattedResult, shopperEmail, false);
            addDetailString(formattedResult, "\n", false);

            getReadOnlyDetails().setText(formattedResult.toString());
        }
    }

    @Nullable
    private Date parseDate(String stringDate) {
        Date result = null;

        //server dates always have format yyyy-mm-dd
        if (!TextUtils.isEmpty(stringDate)) {
            String[] vector = stringDate.split("-");
            try {
                int year = Integer.parseInt(vector[0]);
                int month = Integer.parseInt(vector[1]);
                int day = Integer.parseInt(vector[2]);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);

                result = cal.getTime();
            } catch (Exception e) {
                Log.e(TAG, "parsing date failed", e);
            }
        }

        return result;
    }

    private void addDetailString(@NonNull StringBuilder stringBuilder, @NonNull String detailValue, boolean addTailingSpace) {
        if (!detailValue.isEmpty()) {
            if (addTailingSpace) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(detailValue);
        }
    }

    /**
     * Hide the internal SSN field if there is already an external field for Lookup.
     */
    public void setExternalSsnField() {
        mSocialSecurityNumberLayout.setVisibility(GONE);
        mControllerMap.remove(PersonalDetails.KEY_SOCIAL_SECURITY_NUMBER);
    }

    private void populateGenderSpinner(InputDetail genderDetail) {
        Adapter<Item> adapter = Adapter.forSpinner(new Adapter.TextDelegate<Item>() {
            @NonNull
            @Override
            public String getText(@NonNull Item item) {
                return parseGenderString(item.getId());
            }
        });
        adapter.setViewCustomizationDelegate(new Adapter.ViewCustomizationDelegate() {
            @Override
            public void customizeView(@NonNull CheckedTextView textView) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.primarySpinnerTextSize));
            }
        });
        adapter.setItems(genderDetail.getItems());
        mGender.setAdapter(adapter);

        //set selected value
        if (!TextUtils.isEmpty(genderDetail.getValue())) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (genderDetail.getValue().equals(adapter.getItem(i).getId())) {
                    mGender.setSelection(i);
                    return;
                }
            }
        }
    }

    @NonNull
    private String parseGenderString(String genderKey) {
        //Gender spinner items are not localized from server
        switch (genderKey) {
            case "M":
                return getContext().getResources().getString(R.string.checkout_personal_details_gender_male);
            case "F":
                return getContext().getResources().getString(R.string.checkout_personal_details_gender_female);
            default:
                return getContext().getResources().getString(R.string.checkout_personal_details_gender_unknown);
        }
    }

    /**
     * Get the filled {@link PersonalDetails} object with the data input from the user.
     *
     * @param externalSsn The value of the SSN from an external input if it was already provided.
     * @return The filled {@link PersonalDetails} object, returns null if {@link #isValid()}} if false.
     */
    @Nullable
    public PersonalDetails getPersonalDetails(@Nullable String externalSsn) {
        if (!isValid()) {
            return null;
        }

        if (getFormVisibility() != FieldSetConfiguration.FieldVisibility.EDITABLE) {
            return mReadOnlyPersonalDetails;
        }

        final String firstName = getTextFromInput(mFirstName);
        final String lastName = getTextFromInput(mLastName);
        final String telephone = getTextFromInput(mTelephoneNumber);
        final String email = getTextFromInput(mShopperEmail);

        if (firstName == null || lastName == null || telephone == null || email == null) {
            return null;
        }

        PersonalDetails.Builder builder = new PersonalDetails.Builder(
                firstName, lastName, telephone, email
        );

        //Fields that might not be required
        final String gender = getIdFromItemSpinner(mGender);
        final Date dateOfBirth = mDateOfBirth.getDate();
        final String ssn;
        if (externalSsn != null) {
            ssn = externalSsn;
        } else {
            ssn = getTextFromInput(mSocialSecurityNumber);
        }

        builder.setSocialSecurityNumber(ssn);
        builder.setGender(gender);
        builder.setDateOfBirth(dateOfBirth);

        return builder.build();
    }

    /**
     * Fill in the response from the SSN Lookup.
     * @param ssnLookupName The Name to add to the fields.
     */
    public void fillSsnLookupName(@NonNull Name ssnLookupName) {
        mFirstName.setText(ssnLookupName.getFirstName());
        mLastName.setText(ssnLookupName.getLastName());
    }
}
