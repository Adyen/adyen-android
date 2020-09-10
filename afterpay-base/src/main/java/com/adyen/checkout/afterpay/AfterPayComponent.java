/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 7/11/2019.
 */

package com.adyen.checkout.afterpay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.component.PaymentComponentProviderImpl;
import com.adyen.checkout.base.model.paymentmethods.InputDetail;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.Address;
import com.adyen.checkout.base.model.payments.request.AfterPayPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.base.model.payments.request.ShopperName;
import com.adyen.checkout.base.util.DateUtils;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.base.util.ValidationUtils;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.Collections;
import java.util.List;

public class AfterPayComponent extends BasePaymentComponent<AfterPayConfiguration, AfterPayInputData, AfterPayOutputData, PaymentComponentState> {
    private static final String TAG = LogUtil.getTag();

    private static final String PERSONAL_DETAILS_KEY = "personalDetails";
    private static final String BILLING_ADDRESS_KEY = "billingAddress";
    private static final String DELIVERY_ADDRESS_KEY = "deliveryAddress";
    private static final String SEPARATE_DELIVERY_ADDRESS_KEY = "separateDeliveryAddress";

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String GENDER_KEY = "gender";
    private static final String DATE_OF_BIRTH = "dateOfBirth";
    private static final String SHOPPER_EMAIL_KEY = "shopperEmail";
    private static final String TELEPHONE_NUMBER_KEY = "telephoneNumber";

    private static final String STREET_KEY = "street";
    private static final String HOUSE_NUMBER_KEY = "houseNumberOrName";
    private static final String CITY_KEY = "city";
    private static final String POSTAL_CODE_KEY = "postalCode";
    private static final String STATE_KEY = "stateOrProvince";
    private static final String COUNTRY_KEY = "country";

    public static final PaymentComponentProvider<AfterPayComponent, AfterPayConfiguration> PROVIDER = new PaymentComponentProviderImpl<>(
            AfterPayComponent.class);

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.AFTER_PAY};

    private final AfterPayInputData mInitInputData = new AfterPayInputData();
    private final AfterPayPersonalDataInputData mInitPersonalDataInputData = new AfterPayPersonalDataInputData();
    private final AfterPayAddressInputData mInitBillingAddressInputData = new AfterPayAddressInputData();
    private final AfterPayAddressInputData mInitDeliveryAddressInputData = new AfterPayAddressInputData();

    /**
     * Constructs a {@link AfterPayComponent} object.
     *
     * @param paymentMethod {@link PaymentMethod} represents card payment method.
     * @param configuration {@link AfterPayConfiguration}.
     */
    public AfterPayComponent(@NonNull PaymentMethod paymentMethod, @NonNull AfterPayConfiguration configuration) {
        super(paymentMethod, configuration);

        if (paymentMethod.getDetails() != null) {
            for (InputDetail eachDetail : paymentMethod.getDetails()) {
                final String key = eachDetail.getKey();
                final String value = eachDetail.getValue();
                List<InputDetail> inputDetails = eachDetail.getDetails();

                if (inputDetails == null) {
                    inputDetails = Collections.emptyList();
                }

                if (key != null) {
                    handleInputDetails(key, value, inputDetails);
                }
            }
        }

        mInitInputData.setPersonalDataInputData(mInitPersonalDataInputData);
        mInitInputData.setBillingAddressInputData(mInitBillingAddressInputData);
        mInitInputData.setDeliveryAddressInputData(mInitDeliveryAddressInputData);
    }

    @NonNull
    public AfterPayInputData getInitInputData() {
        return mInitInputData;
    }

    @NonNull
    @Override
    protected AfterPayOutputData onInputDataChanged(@NonNull AfterPayInputData inputData) {
        Logger.v(TAG, "onInputDataChanged");

        final AfterPayOutputData afterPayOutputData = new AfterPayOutputData();

        final AfterPayPersonalDataInputData personalDataInputData = inputData.getPersonalDataInputData();
        final AfterPayAddressInputData billingAddressInputData = inputData.getBillingAddressInputData();
        final AfterPayAddressInputData deliveryAddressInputData = inputData.getDeliveryAddressInputData();

        if (personalDataInputData != null) {
            final AfterPayPersonalDataOutputData personalDataOutputData = validatePersonalInputData(personalDataInputData);
            afterPayOutputData.setAfterPayPersonalDataOutputData(personalDataOutputData);
        }

        if (billingAddressInputData != null) {
            final AfterPayAddressOutputData billingAddressOutputData = validateAddressInputData(billingAddressInputData);
            afterPayOutputData.setBillingAddressOutputData(billingAddressOutputData);
        }

        if (inputData.isSeparateDeliveryAddressEnable() && deliveryAddressInputData != null) {
            final AfterPayAddressOutputData deliveryAddressOutputData = validateAddressInputData(deliveryAddressInputData);
            afterPayOutputData.setDeliveryAddressOutputData(deliveryAddressOutputData);
        } else {
            afterPayOutputData.setDeliveryAddressOutputData(afterPayOutputData.getBillingAddressOutputData());
        }

        afterPayOutputData.setAgreementChecked(inputData.isAgreementChecked());
        afterPayOutputData.setSeparateDeliveryAddress(inputData.isSeparateDeliveryAddressEnable());

        return afterPayOutputData;
    }

    @NonNull
    @Override
    protected PaymentComponentState createComponentState() {
        final AfterPayOutputData afterPayOutputData = getOutputData();

        final PaymentComponentData<AfterPayPaymentMethod> paymentComponentData = new PaymentComponentData<>();
        final AfterPayPaymentMethod paymentMethod = new AfterPayPaymentMethod();

        paymentMethod.setType(AfterPayPaymentMethod.PAYMENT_METHOD_TYPE);

        if (afterPayOutputData != null) {
            paymentMethod.setConsentCheckbox(afterPayOutputData.isAgreementChecked());

            final AfterPayPersonalDataOutputData personalData = afterPayOutputData.getAfterPayPersonalDataOutputData();

            final ShopperName shopperName = new ShopperName();
            final Gender gender = personalData.getGenderField().getValue();
            shopperName.setGender(gender.getValue());
            shopperName.setFirstName(personalData.getFirstNameField().getValue());
            shopperName.setLastName(personalData.getLastNameField().getValue());

            paymentComponentData.setShopperName(shopperName);
            paymentComponentData.setDateOfBirth(DateUtils.toServerDateFormat(personalData.getDateOfBirthField().getValue()));
            paymentComponentData.setTelephoneNumber(personalData.getTelephoneNumberField().getValue());
            paymentComponentData.setShopperEmail(personalData.getShopperEmailField().getValue());

            paymentComponentData.setDeliveryAddress(getAddressRequestFromOutputData(afterPayOutputData.getDeliveryAddressOutputData()));
            paymentComponentData.setBillingAddress(getAddressRequestFromOutputData(afterPayOutputData.getBillingAddressOutputData()));
        }

        paymentComponentData.setPaymentMethod(paymentMethod);

        return new PaymentComponentState<>(paymentComponentData, afterPayOutputData != null && afterPayOutputData.isValid());
    }

    @NonNull
    @Override
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }

    private Address getAddressRequestFromOutputData(AfterPayAddressOutputData billingAddressOutputData) {
        final Address address = new Address();
        address.setStreet(billingAddressOutputData.getStreet().getValue());
        address.setStateOrProvince(billingAddressOutputData.getStateOrProvince().getValue());
        address.setPostalCode(billingAddressOutputData.getPostalCode().getValue());
        address.setHouseNumberOrName(billingAddressOutputData.getHouseNumberOrName().getValue());
        address.setCity(billingAddressOutputData.getCity().getValue());
        address.setCountry(billingAddressOutputData.getLocale().getValue().getCountry());

        return address;
    }

    private AfterPayPersonalDataOutputData validatePersonalInputData(AfterPayPersonalDataInputData personalDataInputData) {
        return new AfterPayPersonalDataOutputData(
                validateString(personalDataInputData.getFirstName()),
                validateString(personalDataInputData.getLastName()),
                // no need for validation
                new ValidatedField<>(personalDataInputData.getGender(), ValidatedField.Validation.VALID),
                new ValidatedField<>(personalDataInputData.getDateOfBirth(), ValidatedField.Validation.VALID),
                validateString(personalDataInputData.getTelephoneNumber(),
                        ValidationUtils.isPhoneNumberValid(personalDataInputData.getTelephoneNumber())),
                validateString(personalDataInputData.getShopperEmail(), ValidationUtils.isEmailValid(personalDataInputData.getShopperEmail())
                )
        );
    }

    private AfterPayAddressOutputData validateAddressInputData(AfterPayAddressInputData addressInputData) {
        return new AfterPayAddressOutputData(
                validateString(addressInputData.getStreet()),
                validateString(addressInputData.getHouseNumberOrName()),
                validateString(addressInputData.getCity()),
                validateString(addressInputData.getPostalCode()),
                optionalString(addressInputData.getStateOrProvince()),
                new ValidatedField<>(addressInputData.getLocale(), ValidatedField.Validation.VALID)
        );
    }

    private ValidatedField<String> optionalString(String value) {
        return new ValidatedField<>(value, ValidatedField.Validation.VALID);
    }

    private ValidatedField<String> validateString(String value) {
        return this.validateString(value, true);
    }

    private ValidatedField<String> validateString(String value, boolean otherConditionResult) {
        return new ValidatedField<>(value,
                !TextUtils.isEmpty(value) && otherConditionResult ? ValidatedField.Validation.VALID : ValidatedField.Validation.PARTIAL);
    }

    private void handleInputDetails(@NonNull String key, @Nullable String value, @NonNull List<InputDetail> eachInputDetails) {
        switch (key) {
            case PERSONAL_DETAILS_KEY:
                handlePersonalDetails(eachInputDetails);
                break;
            case BILLING_ADDRESS_KEY:
                handleAddressDetails(mInitBillingAddressInputData, eachInputDetails);
                break;
            case SEPARATE_DELIVERY_ADDRESS_KEY:
                mInitInputData.setSeparateDeliveryAddress(Boolean.parseBoolean(value));
                break;
            case DELIVERY_ADDRESS_KEY:
                handleAddressDetails(mInitDeliveryAddressInputData, eachInputDetails);
                break;
            default:
                Logger.i(TAG, "unrecognized key");
                break;
        }
    }

    private void handlePersonalDetails(@NonNull List<InputDetail> personalDetails) {
        for (InputDetail eachDetail : personalDetails) {
            final String key = eachDetail.getKey();
            final String value = eachDetail.getValue();

            if (key != null && value != null) {
                switch (key) {
                    case FIRST_NAME_KEY:
                        mInitPersonalDataInputData.setFirstName(value);
                        break;
                    case LAST_NAME_KEY:
                        mInitPersonalDataInputData.setLastName(value);
                        break;
                    case GENDER_KEY:
                        mInitPersonalDataInputData.setGender(Gender.valueOf(value));
                        break;
                    case DATE_OF_BIRTH:
                        mInitPersonalDataInputData.setDateOfBirth(DateUtils.parseServerDateFormat(value));
                        break;
                    case SHOPPER_EMAIL_KEY:
                        mInitPersonalDataInputData.setShopperEmail(value);
                        break;
                    case TELEPHONE_NUMBER_KEY:
                        mInitPersonalDataInputData.setTelephoneNumber(value);
                        break;
                    default:
                        Logger.i(TAG, "unrecognized key");
                        break;
                }
            }
        }
    }

    private void handleAddressDetails(@NonNull AfterPayAddressInputData inputAfterPayAddressInputData,
            @NonNull List<InputDetail> billingAddressDetails) {
        for (InputDetail eachDetail : billingAddressDetails) {
            final String key = eachDetail.getKey();
            final String value = eachDetail.getValue();

            if (key != null && value != null) {
                switch (key) {
                    case STREET_KEY:
                        inputAfterPayAddressInputData.setStreet(value);
                        break;
                    case HOUSE_NUMBER_KEY:
                        inputAfterPayAddressInputData.setHouseNumberOrName(value);
                        break;
                    case CITY_KEY:
                        inputAfterPayAddressInputData.setCity(value);
                        break;
                    case POSTAL_CODE_KEY:
                        inputAfterPayAddressInputData.setPostalCode(value);
                        break;
                    case STATE_KEY:
                        inputAfterPayAddressInputData.setStateOrProvince(value);
                        break;
                    case COUNTRY_KEY:
                        inputAfterPayAddressInputData.setLocale(getConfiguration().getCountryCode().getLocale());
                        break;
                    default:
                        Logger.i(TAG, "unrecognized key");
                        break;
                }
            }
        }
    }
}
