package com.adyen.customwithcheckoutui;

import android.support.annotation.NonNull;

import com.adyen.core.models.Amount;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentSetupRequest {

    private Amount amount;
    private String merchantReference;
    private String shopperIP;
    private String shopperLocale;
    private String merchantAccount;
    private String countryCode;
    private String paymentDeadline;
    private String returnURL;
    private String paymentToken;

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(final Amount amount) {
        this.amount = amount;
    }

    public String getMerchantReference() {
        return merchantReference;
    }

    public void setMerchantReference(final String merchantReference) {
        this.merchantReference = merchantReference;
    }

    public String getShopperIP() {
        return shopperIP;
    }

    public void setShopperIP(final String shopperIP) {
        this.shopperIP = shopperIP;
    }

    public String getShopperLocale() {
        return shopperLocale;
    }

    public void setShopperLocale(final String shopperLocale) {
        this.shopperLocale = shopperLocale;
    }

    public String getMerchantAccount() {
        return merchantAccount;
    }

    public void setMerchantAccount(final String merchantAccount) {
        this.merchantAccount = merchantAccount;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(final String paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public String getReturnURL() {
        return returnURL;
    }

    public void setReturnURL(final String returnURL) {
        this.returnURL = returnURL;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(final String paymentToken) {
        this.paymentToken = paymentToken;
    }

    @NonNull
    public String getSetupDataString() {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reference", merchantReference);
            jsonObject.put("merchantAccount", merchantAccount);
            jsonObject.put("shopperLocale", shopperLocale);

            jsonObject.put("appUrl", returnURL);
            jsonObject.put("countryCode", countryCode);
            jsonObject.put("sessionValidity", paymentDeadline);

            final JSONObject paymentAmount = new JSONObject();
            paymentAmount.put("currency", amount.getCurrency());
            paymentAmount.put("value", amount.getValue());
            jsonObject.put("amount", paymentAmount);

            // HACK
            jsonObject.put("shopperReference", "aap");

            //Device fingerprint
            jsonObject.put("sdkToken", paymentToken);


        } catch (final JSONException jsonException) {
            //TODO: What to do?
        }

        return jsonObject.toString();
    }

}
