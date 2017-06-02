package com.adyen.core.constants;

public class Constants {

    public static class PaymentRequest {
        public static final String PAYMENT_METHOD_SELECTED_INTENT = "com.adyen.core.ui.PaymentMethodSelected";
        public static final String PAYMENT_REQUEST_CANCELLED_INTENT = "com.adyen.core.ui.PaymentRequestCancelled";
        public static final String ADYEN_UI_FINALIZE_INTENT = "com.adyen.core.ui.finish";
        public static final String REDIRECT_HANDLED_INTENT = "com.adyen.core.RedirectHandled";
        public static final String REDIRECT_PROBLEM_INTENT = "com.adyen.core.RedirectProblem";
        public static final String REDIRECT_RETURN_URI_KEY = "returnUri";
        public static final String PAYMENT_DETAILS_PROVIDED_INTENT = "com.adyen.core.ui.PaymentDetailsProvided";
        public static final String PAYMENT_DETAILS = "PaymentDetails";
    }

    public static class PaymentStateHandler {
        public static final String SDK_RETURN_URL = "adyencheckout://";
        public static final String REDIRECT_RESPONSE = "redirect";
        public static final String COMPLETE_RESPONSE = "complete";
        public static final String ERROR_RESPONSE = "error";
        public static final String URL_JSON_KEY = "url";
        public static final String ANDROID_PAY_TOKEN_PROVIDED = "com.adyen.androidpay.ui.AndroidTokenProvided";
    }

    public static class DataKeys {
        public static final String PUBLIC_KEY = "public_key";
        public static final String GENERATION_TIME = "generation_time";
        public static final String SHOPPER_REFERENCE = "shopper_reference";
        public static final String AMOUNT = "amount";
        public static final String PAYMENT_METHOD = "PaymentMethod";
    }

}
