package com.adyen.core.exceptions;

/**
 * Exception to notify that classes from the adyen-ui module could not be loaded.
 */
public class UIModuleNotAvailableException extends Exception {

    public UIModuleNotAvailableException(String message) {
        super(message);
    }
}
