package com.adyen.core.exceptions;

public class PaymentMethodNotSupportedException extends Exception {

    public PaymentMethodNotSupportedException(String message) {
        super(message);
    }

}
