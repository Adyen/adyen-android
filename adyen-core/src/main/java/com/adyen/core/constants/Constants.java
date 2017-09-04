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

    public static class DataKeys {
        public static final String PUBLIC_KEY = "public_key";
        public static final String GENERATION_TIME = "generation_time";
        public static final String SHOPPER_REFERENCE = "shopper_reference";
        public static final String AMOUNT = "amount";
        public static final String PAYMENT_METHOD = "PaymentMethod";
        public static final String CVC_FIELD_STATUS = "cvc_field_status";
        public static final String PAYMENT_CARD_SCAN_ENABLED = "payment_card_scan_enabled";
    }

}
