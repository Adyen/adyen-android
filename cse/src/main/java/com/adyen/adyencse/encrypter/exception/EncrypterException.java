package com.adyen.adyencse.encrypter.exception;

/**
 * Created by andrei on 8/8/16.
 */
public class EncrypterException extends Exception {

    private static final long serialVersionUID = 2699577096011945291L;

    /**
     * Wrapping exception for all JCE encryption related exceptions
     * @param message
     * @param cause
     */
    public EncrypterException(String message, Throwable cause) {
        super(message, cause);
    }

}
