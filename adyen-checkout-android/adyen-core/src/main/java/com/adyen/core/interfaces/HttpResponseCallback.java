package com.adyen.core.interfaces;

/**
 * Communicates the response from a HTTP request.
 */
public interface HttpResponseCallback {

    /**
     * Callback for successful HTTP requests.
     * @param response Response body of the successful HTTP request
     *                 Successful response codes:
     *                 {@link java.net.HttpURLConnection#HTTP_OK}
     *                 {@link java.net.HttpURLConnection#HTTP_CREATED}
     *                 {@link java.net.HttpURLConnection#HTTP_ACCEPTED}
     */
    void onSuccess(byte[] response);

    /**
     * Callback for HTTP Request failures.
     * @param e error that caused the request failure
     */
    void onFailure(Throwable e);

}
