package com.adyen.core.models.paymentdetails;

import java.util.Collection;

/**
 * {@link PaymentDetails} class for credit card Payments.
 * This class contains only encrypted card data.
 */
public class CreditCardPaymentDetails extends PaymentDetails {

    public static final String ADDITIONAL_DATA_CARD = "additionalData.card.encrypted.json";
    public static final String STORE_DETAILS = "storeDetails";
    public static final String INSTALLMENTS = "installments";
    public static final String BILLING_ADDRESS = "billingAddress";

    public static final String CARD_HOLDER_NAME_REQUIRED = "cardHolderNameRequired";

    public enum AddressKey {
        street,
        houseNumberOrName,
        city,
        country,
        postalCode,
        stateOrProvince;
    }

    public CreditCardPaymentDetails(Collection<InputDetail> inputDetails) {
        super(inputDetails);
    }

    public boolean fillCardToken(final String cardToken) {
        return super.fill(ADDITIONAL_DATA_CARD, cardToken);
    }

    public boolean fillStoreDetails(final boolean storeDetails) {
        return super.fill(STORE_DETAILS, storeDetails);
    }

    public boolean fillNumberOfInstallments(final short numberOfInstallments) {
        return super.fill(INSTALLMENTS, String.valueOf(numberOfInstallments));
    }

    public boolean fillbillingAddressStreet(String street) {
        for (InputDetail inputDetail : getInputDetails()) {
            if (inputDetail.getKey().equals(BILLING_ADDRESS)) {
                for (InputDetail addressDetail : inputDetail.getInputDetails()) {
                    if (addressDetail.getKey().equals(AddressKey.street.name())) {
                        return addressDetail.fill(street);
                    }
                }
            }
        }
        return false;
    }

    public boolean fillbillingAddressHouseNumberOrName(String houseNumberOrName) {
        for (InputDetail inputDetail : getInputDetails()) {
            if (inputDetail.getKey().equals(BILLING_ADDRESS)) {
                for (InputDetail addressDetail : inputDetail.getInputDetails()) {
                    if (addressDetail.getKey().equals(AddressKey.houseNumberOrName.name())) {
                        return addressDetail.fill(houseNumberOrName);
                    }
                }
            }
        }
        return false;
    }

    public boolean fillbillingAddressCity(String city) {
        for (InputDetail inputDetail : getInputDetails()) {
            if (inputDetail.getKey().equals(BILLING_ADDRESS)) {
                for (InputDetail addressDetail : inputDetail.getInputDetails()) {
                    if (addressDetail.getKey().equals(AddressKey.city.name())) {
                        return addressDetail.fill(city);
                    }
                }
            }
        }
        return false;
    }

    public boolean fillbillingAddressCountry(String country) {
        for (InputDetail inputDetail : getInputDetails()) {
            if (inputDetail.getKey().equals(BILLING_ADDRESS)) {
                for (InputDetail addressDetail : inputDetail.getInputDetails()) {
                    if (addressDetail.getKey().equals(AddressKey.country.name())) {
                        return addressDetail.fill(country);
                    }
                }
            }
        }
        return false;
    }

    public boolean fillbillingAddressPostalCode(String postalCode) {
        for (InputDetail inputDetail : getInputDetails()) {
            if (inputDetail.getKey().equals(BILLING_ADDRESS)) {
                for (InputDetail addressDetail : inputDetail.getInputDetails()) {
                    if (addressDetail.getKey().equals(AddressKey.postalCode.name())) {
                        return addressDetail.fill(postalCode);
                    }
                }
            }
        }
        return false;
    }

    public boolean fillbillingAddressStateOrProvince(String stateOrProvince) {
        for (InputDetail inputDetail : getInputDetails()) {
            if (inputDetail.getKey().equals(BILLING_ADDRESS)) {
                for (InputDetail addressDetail : inputDetail.getInputDetails()) {
                    if (addressDetail.getKey().equals(AddressKey.stateOrProvince.name())) {
                        return addressDetail.fill(stateOrProvince);
                    }
                }
            }
        }
        return false;
    }

}
