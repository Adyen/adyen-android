package com.adyen.core.models;

import android.support.annotation.NonNull;

/**
 * Payment modules.
 */

public enum PaymentModule {

    androidpay {
        @NonNull
        @Override
        public String getServiceName() {
            return "com.adyen.androidpay.AndroidPayService";
        }

        @NonNull
        @Override
        public String getInputFieldsServiceName() {
            return null;
        }
    },

    applepay {
        @NonNull
        @Override
        public String getServiceName() {
            return "ApplePayService";
        }

        @NonNull
        @Override
        public String getInputFieldsServiceName() {
            return null;
        }
    },

    samsungpay {
        @NonNull
        @Override
        public String getServiceName() {
            return "com.adyen.samsungpay.SamsungPayService";
        }

        @NonNull
        @Override
        public String getInputFieldsServiceName() {
            return null;
        }
    };

    @NonNull
    public abstract String getServiceName();

    @NonNull
    public abstract String getInputFieldsServiceName();

}
