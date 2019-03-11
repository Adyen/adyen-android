/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 08/08/2017.
 */

package com.adyen.checkout.core.internal.model;

import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.AdditionalDetails;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.AuthenticationDetails;
import com.adyen.checkout.core.PaymentResult;
import com.adyen.checkout.core.RedirectDetails;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.model.Authentication;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentResultCode;
import com.adyen.checkout.core.model.RedirectData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class PaymentInitiationResponse extends JsonObject {
    @NonNull
    public static final Creator<PaymentInitiationResponse> CREATOR = new DefaultCreator<>(PaymentInitiationResponse.class);

    private static final String KEY_TYPE = "type";

    private final Type mType;

    private final CompleteFields mCompleteFields;

    private final AuthenticationFields mAuthenticationFields;

    private final DetailFields mDetailFields;

    private final RedirectFields mRedirectFields;

    private final ErrorFields mErrorFields;

    private PaymentInitiationResponse(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mType = parseEnum(KEY_TYPE, Type.class);

        switch (mType) {
            case COMPLETE:
                mCompleteFields = parseFrom(jsonObject, CompleteFields.class);
                mAuthenticationFields = null;
                mDetailFields = null;
                mRedirectFields = null;
                mErrorFields = null;
                break;
            case DETAILS:
                mCompleteFields = null;
                mAuthenticationFields = null;
                mDetailFields = parseFrom(jsonObject, DetailFields.class);
                mRedirectFields = null;
                mErrorFields = null;
                break;
            case REDIRECT:
                mCompleteFields = null;
                mAuthenticationFields = null;
                mDetailFields = null;
                mRedirectFields = parseFrom(jsonObject, RedirectFields.class);
                mErrorFields = null;
                break;
            case IDENTIFY_SHOPPER:
            case CHALLENGE_SHOPPER:
                mCompleteFields = null;
                mAuthenticationFields = parseFrom(jsonObject, AuthenticationFields.class);
                mDetailFields = null;
                mRedirectFields = null;
                mErrorFields = null;
                break;
            case ERROR:
            case VALIDATION:
                mCompleteFields = null;
                mAuthenticationFields = null;
                mDetailFields = null;
                mRedirectFields = null;
                mErrorFields = parseFrom(jsonObject, ErrorFields.class);
                break;
            default:
                throw new RuntimeException("Unknown type: " + mType);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentInitiationResponse that = (PaymentInitiationResponse) o;

        if (mType != that.mType) {
            return false;
        }
        if (mCompleteFields != null ? !mCompleteFields.equals(that.mCompleteFields) : that.mCompleteFields != null) {
            return false;
        }
        if (mAuthenticationFields != null ? !mAuthenticationFields.equals(that.mAuthenticationFields) : that.mAuthenticationFields != null) {
            return false;
        }
        if (mDetailFields != null ? !mDetailFields.equals(that.mDetailFields) : that.mDetailFields != null) {
            return false;
        }
        if (mRedirectFields != null ? !mRedirectFields.equals(that.mRedirectFields) : that.mRedirectFields != null) {
            return false;
        }
        return mErrorFields != null ? mErrorFields.equals(that.mErrorFields) : that.mErrorFields == null;
    }

    @Override
    public int hashCode() {
        int result = mType != null ? mType.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mCompleteFields != null ? mCompleteFields.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mDetailFields != null ? mDetailFields.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mRedirectFields != null ? mRedirectFields.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mErrorFields != null ? mErrorFields.hashCode() : 0);
        return result;
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    @Nullable
    public CompleteFields getCompleteFields() {
        return mCompleteFields;
    }

    @Nullable
    public AuthenticationFields getAuthenticationFields() {
        return mAuthenticationFields;
    }

    @Nullable
    public DetailFields getDetailFields() {
        return mDetailFields;
    }

    @Nullable
    public RedirectFields getRedirectFields() {
        return mRedirectFields;
    }

    @Nullable
    public ErrorFields getErrorFields() {
        return mErrorFields;
    }

    public enum Type {
        COMPLETE,
        DETAILS,
        REDIRECT,
        @SerializedName("identifyShopper")
        IDENTIFY_SHOPPER,
        @SerializedName("challengeShopper")
        CHALLENGE_SHOPPER,
        ERROR,
        VALIDATION
    }

    public static final class CompleteFields extends JsonObject implements PaymentResult {
        @NonNull
        public static final Parcelable.Creator<CompleteFields> CREATOR = new DefaultCreator<>(CompleteFields.class);

        private static final String KEY_PAYLOAD = "payload";

        private static final String KEY_PAYMENT_METHOD = "paymentMethod";

        private static final String KEY_RESULT_CODE = "resultCode";

        private final String mPayload;

        private final PaymentMethodBase mPaymentMethod;

        private final PaymentResultCode mResultCode;

        private CompleteFields(@NonNull JSONObject jsonObject) throws JSONException {
            super(jsonObject);

            mPayload = jsonObject.getString(KEY_PAYLOAD);
            mPaymentMethod = parseOptional(KEY_PAYMENT_METHOD, PaymentMethodBase.class);
            mResultCode = parseEnum(KEY_RESULT_CODE, PaymentResultCode.class);
        }

        @NonNull
        @Override
        public String getPayload() {
            return mPayload;
        }

        @NonNull
        @Override
        public PaymentResultCode getResultCode() {
            return mResultCode;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CompleteFields that = (CompleteFields) o;

            if (mPayload != null ? !mPayload.equals(that.mPayload) : that.mPayload != null) {
                return false;
            }
            if (mResultCode != that.mResultCode) {
                return false;
            }
            return mPaymentMethod != null ? mPaymentMethod.equals(that.mPaymentMethod) : that.mPaymentMethod == null;
        }

        @Override
        public int hashCode() {
            int result = mPayload != null ? mPayload.hashCode() : 0;
            result = HashUtils.MULTIPLIER * result + (mResultCode != null ? mResultCode.hashCode() : 0);
            result = HashUtils.MULTIPLIER * result + (mPaymentMethod != null ? mPaymentMethod.hashCode() : 0);
            return result;
        }

        @Nullable
        public PaymentMethodBase getPaymentMethod() {
            return mPaymentMethod;
        }
    }

    public static final class DetailFields extends JsonObject implements AdditionalDetails {
        @NonNull
        public static final Parcelable.Creator<DetailFields> CREATOR = new DefaultCreator<>(DetailFields.class);

        private static final String KEY_PAYMENT_METHOD = "paymentMethod";

        private static final String KEY_PAYMENT_METHOD_RETURN_DATA = "paymentMethodReturnData";

        private static final String KEY_REDIRECT_DATA = "redirectData";

        private static final String KEY_RESPONSE_DETAILS = "responseDetails";

        private final PaymentMethodBase mPaymentMethod;

        private final String mPaymentMethodReturnData;

        private final JSONObject mRedirectData;

        private final List<InputDetailImpl> mResponseDetails;

        private DetailFields(@NonNull JSONObject jsonObject) throws JSONException {
            super(jsonObject);

            mPaymentMethod = JsonObject.parseFrom(jsonObject.getJSONObject(KEY_PAYMENT_METHOD), PaymentMethodBase.class);
            mPaymentMethodReturnData = jsonObject.optString(KEY_PAYMENT_METHOD_RETURN_DATA);
            mRedirectData = jsonObject.getJSONObject(KEY_REDIRECT_DATA);
            mResponseDetails = parseList(KEY_RESPONSE_DETAILS, InputDetailImpl.class);
        }

        @NonNull
        @Override
        public String getPaymentMethodType() {
            return mPaymentMethod.getType();
        }

        @NonNull
        @Override
        public List<InputDetail> getInputDetails() {
            List<InputDetail> inputDetails = new ArrayList<>();

            for (InputDetail responseDetail : mResponseDetails) {
                String responseDetailKey = responseDetail.getKey();
                boolean isNotInRedirectData = !mRedirectData.has(responseDetailKey);
                boolean isNotPaymentMethodReturnData = !KEY_PAYMENT_METHOD_RETURN_DATA.equals(responseDetailKey);

                if (isNotInRedirectData && isNotPaymentMethodReturnData) {
                    inputDetails.add(responseDetail);
                }
            }

            return inputDetails;
        }

        @NonNull
        @Override
        public <T extends RedirectData> T getRedirectData(@NonNull Class<T> redirectDataClass) throws CheckoutException {
            if (mRedirectData == null) {
                throw new CheckoutException.Builder("No RedirectData is available.", null).build();
            }

            return ProvidedBy.Util.parse(mRedirectData, redirectDataClass);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DetailFields that = (DetailFields) o;

            if (mPaymentMethod != null ? !mPaymentMethod.equals(that.mPaymentMethod) : that.mPaymentMethod != null) {
                return false;
            }
            if (mPaymentMethodReturnData != null ? !mPaymentMethodReturnData.equals(that.mPaymentMethodReturnData) : that.mPaymentMethodReturnData
                    != null) {
                return false;
            }
            if (mRedirectData != null ? !mRedirectData.equals(that.mRedirectData) : that.mRedirectData != null) {
                return false;
            }
            return mResponseDetails != null ? mResponseDetails.equals(that.mResponseDetails) : that.mResponseDetails == null;
        }

        @Override
        public int hashCode() {
            int result = mPaymentMethod != null ? mPaymentMethod.hashCode() : 0;
            result = HashUtils.MULTIPLIER * result + (mPaymentMethodReturnData != null ? mPaymentMethodReturnData.hashCode() : 0);
            result = HashUtils.MULTIPLIER * result + (mRedirectData != null ? mRedirectData.hashCode() : 0);
            result = HashUtils.MULTIPLIER * result + (mResponseDetails != null ? mResponseDetails.hashCode() : 0);
            return result;
        }

        @NonNull
        public PaymentMethodBase getPaymentMethod() {
            return mPaymentMethod;
        }

        @Nullable
        public String getPaymentMethodReturnData() {
            return mPaymentMethodReturnData;
        }

        @NonNull
        public List<InputDetailImpl> getResponseDetails() {
            return mResponseDetails;
        }
    }

    public static final class RedirectFields extends JsonObject implements RedirectDetails {
        @NonNull
        public static final Parcelable.Creator<RedirectFields> CREATOR = new DefaultCreator<>(RedirectFields.class);

        private static final String KEY_PAYMENT_METHOD = "paymentMethod";

        private static final String KEY_URL = "url";

        private static final String KEY_SUBMIT_PAYMENT_METHOD_RETURN_DATA = "submitPaymentMethodReturnData";

        private final PaymentMethodBase mPaymentMethod;

        private final String mUrl;

        private final Boolean mSubmitPaymentMethodReturnData;

        private RedirectFields(@NonNull JSONObject jsonObject) throws JSONException {
            super(jsonObject);

            mPaymentMethod = parseFrom(jsonObject.getJSONObject(KEY_PAYMENT_METHOD), PaymentMethodBase.class);
            mUrl = jsonObject.getString(KEY_URL);
            mSubmitPaymentMethodReturnData = jsonObject.has(KEY_SUBMIT_PAYMENT_METHOD_RETURN_DATA)
                    ? jsonObject.getBoolean(KEY_SUBMIT_PAYMENT_METHOD_RETURN_DATA)
                    : null;
        }

        @NonNull
        @Override
        public Uri getUri() {
            return Uri.parse(mUrl);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RedirectFields that = (RedirectFields) o;

            if (mPaymentMethod != null ? !mPaymentMethod.equals(that.mPaymentMethod) : that.mPaymentMethod != null) {
                return false;
            }
            if (mUrl != null ? !mUrl.equals(that.mUrl) : that.mUrl != null) {
                return false;
            }
            return mSubmitPaymentMethodReturnData != null ? mSubmitPaymentMethodReturnData.equals(that.mSubmitPaymentMethodReturnData) : that
                    .mSubmitPaymentMethodReturnData == null;
        }

        @Override
        public int hashCode() {
            int result = mPaymentMethod != null ? mPaymentMethod.hashCode() : 0;
            result = HashUtils.MULTIPLIER * result + (mUrl != null ? mUrl.hashCode() : 0);
            result = HashUtils.MULTIPLIER * result + (mSubmitPaymentMethodReturnData != null ? mSubmitPaymentMethodReturnData.hashCode() : 0);
            return result;
        }

        @NonNull
        public PaymentMethodBase getPaymentMethod() {
            return mPaymentMethod;
        }

        @NonNull
        public String getUrl() {
            return mUrl;
        }

        public boolean isSubmitPaymentMethodReturnData() {
            return Boolean.TRUE.equals(mSubmitPaymentMethodReturnData);
        }
    }

    public static final class AuthenticationFields extends JsonObject implements AuthenticationDetails {
        public static final Parcelable.Creator<AuthenticationFields> CREATOR = new DefaultCreator<>(AuthenticationFields.class);

        private static final String KEY_AUTHENTICATION = "authentication";

        private static final String KEY_PAYMENT_DATA = "paymentData";

        private static final String KEY_PAYMENT_METHOD = "paymentMethod";

        private static final String KEY_RESPONSE_DETAILS = "responseDetails";

        private static final String KEY_RESULT_CODE = "resultCode";

        private final JSONObject mAuthentication;

        private final String mPaymentData;

        private final PaymentMethodBase mPaymentMethod;

        private final List<InputDetailImpl> mResponseDetails;

        private final PaymentResultCode mResultCode;

        protected AuthenticationFields(@NonNull JSONObject jsonObject) throws JSONException {
            super(jsonObject);

            mPaymentData = jsonObject.getString(KEY_PAYMENT_DATA);
            mPaymentMethod = parse(KEY_PAYMENT_METHOD, PaymentMethodBase.class);
            mAuthentication = jsonObject.getJSONObject(KEY_AUTHENTICATION);
            mResponseDetails = parseList(KEY_RESPONSE_DETAILS, InputDetailImpl.class);
            mResultCode = parseEnum(KEY_RESULT_CODE, PaymentResultCode.class);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AuthenticationFields)) {
                return false;
            }

            AuthenticationFields that = (AuthenticationFields) o;

            if (mAuthentication != null ? !mAuthentication.equals(that.mAuthentication) : that.mAuthentication != null) {
                return false;
            }
            if (mPaymentData != null ? !mPaymentData.equals(that.mPaymentData) : that.mPaymentData != null) {
                return false;
            }
            if (mPaymentMethod != null ? !mPaymentMethod.equals(that.mPaymentMethod) : that.mPaymentMethod != null) {
                return false;
            }
            if (mResponseDetails != null ? !mResponseDetails.equals(that.mResponseDetails) : that.mResponseDetails != null) {
                return false;
            }
            return mResultCode == that.mResultCode;
        }

        @Override
        public int hashCode() {
            int result = mAuthentication != null ? mAuthentication.hashCode() : 0;
            result = HashUtils.MULTIPLIER * result + (mPaymentData != null ? mPaymentData.hashCode() : 0);
            result = HashUtils.MULTIPLIER * result + (mPaymentMethod != null ? mPaymentMethod.hashCode() : 0);
            result = HashUtils.MULTIPLIER * result + (mResponseDetails != null ? mResponseDetails.hashCode() : 0);
            result = HashUtils.MULTIPLIER * result + (mResultCode != null ? mResultCode.hashCode() : 0);
            return result;
        }

        @NonNull
        @Override
        public String getPaymentMethodType() {
            return mPaymentMethod.getType();
        }

        @NonNull
        @Override
        public List<InputDetail> getInputDetails() {
            return new ArrayList<InputDetail>(mResponseDetails);
        }

        @NonNull
        @Override
        public <T extends Authentication> T getAuthentication(@NonNull Class<T> authenticationClass) throws CheckoutException {
            if (authenticationClass == null) {
                throw new CheckoutException.Builder("No Authentication is available.", null).build();
            }

            return ProvidedBy.Util.parse(mAuthentication, authenticationClass);
        }

        @NonNull
        @Override
        public PaymentResultCode getResultCode() {
            return mResultCode;
        }

        @NonNull
        public String getPaymentData() {
            return mPaymentData;
        }
    }

    public static final class ErrorFields extends JsonObject {
        @NonNull
        public static final Creator<ErrorFields> CREATOR = new DefaultCreator<>(ErrorFields.class);

        private static final String KEY_ERROR_CODE = "errorCode";

        private static final String KEY_ERROR_MESSAGE = "errorMessage";

        private static final String KEY_PAYLOAD = "payload";

        private final String mErrorMessage;

        private final ErrorCode mErrorCode;

        private final String mPayload;

        public ErrorFields(@NonNull JSONObject jsonObject) throws JSONException {
            super(jsonObject);

            mErrorCode = parseEnum(KEY_ERROR_CODE, ErrorCode.class);
            mErrorMessage = jsonObject.getString(KEY_ERROR_MESSAGE);
            mPayload = jsonObject.optString(KEY_PAYLOAD, null);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ErrorFields that = (ErrorFields) o;

            if (mErrorMessage != null ? !mErrorMessage.equals(that.mErrorMessage) : that.mErrorMessage != null) {
                return false;
            }
            if (mErrorCode != that.mErrorCode) {
                return false;
            }
            return mPayload != null ? mPayload.equals(that.mPayload) : that.mPayload == null;
        }

        @Override
        public int hashCode() {
            int result = mErrorMessage != null ? mErrorMessage.hashCode() : 0;
            result = HashUtils.MULTIPLIER * result + (mErrorCode != null ? mErrorCode.hashCode() : 0);
            result = HashUtils.MULTIPLIER * result + (mPayload != null ? mPayload.hashCode() : 0);
            return result;
        }

        @Nullable
        public ErrorCode getErrorCode() {
            return mErrorCode;
        }

        @NonNull
        public String getErrorMessage() {
            return mErrorMessage;
        }

        @Nullable
        public String getPayload() {
            return mPayload;
        }
    }

    /**
     * The error code.
     */
    public enum ErrorCode {
        @SerializedName("PI001") EMPTY_REQUEST,
        @SerializedName("PI002") PAYLOAD_NOT_PROVIDED,
        @SerializedName("PI003") INVALID_PAYLOAD,
        @SerializedName("PI004") PAYMENT_METHOD_DATA_NOT_PROVIDED,
        @SerializedName("PI005") INVALID_PAYMENT_METHOD_DATA,
        @SerializedName("PI006") INVALID_PAYMENT_METHOD_DETAILS,
        @SerializedName("PI007") PAYMENT_SESSION_EXPIRED,
        @SerializedName("PI008") NOT_ALLOWED,
        @SerializedName("PI009") INCOMPLETE_PAYMENTS_REQUEST,
        @SerializedName("PI010") INVALID_EXPIRY_DATE,
        @SerializedName("PI011") INVALID_SECURITY_CODE_LENGTH,
        @SerializedName("PI012") INVALID_BIC,
        @SerializedName("PI013") ISSUER_NOT_PROVIDED,
        @SerializedName("PI101") INTERNAL_ERROR,
        @SerializedName("PI102") PAYMENT_ERROR
    }
}
