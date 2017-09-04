package com.adyen.core.internals;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.core.exceptions.HttpAuthenticationException;
import com.adyen.core.exceptions.HttpAuthorizationException;
import com.adyen.core.exceptions.HttpDownForMaintenanceException;
import com.adyen.core.exceptions.HttpServerException;
import com.adyen.core.exceptions.UnexpectedException;
import com.adyen.core.utils.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class HttpClient<T extends HttpClient> {

    private static final @NonNull String METHOD_GET = "GET";
    private static final @NonNull String METHOD_POST = "POST";

    @Nullable
    private SSLSocketFactory sslSocketFactory;
    private int connectTimeout;
    private int readTimeout;


    public HttpClient() {
        connectTimeout = (int) TimeUnit.SECONDS.toMillis(60);
        readTimeout = (int) TimeUnit.SECONDS.toMillis(60);

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (final @NonNull NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            sslSocketFactory = null;
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public T setConnectTimeout(int timeout) {
        connectTimeout = timeout;
        return (T) this;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public T setReadTimeout(int timeout) {
        readTimeout = timeout;
        return (T) this;
    }

    /**
     * Synchronous POST request.
     *
     * @param url server URL
     * @param data the body of the POST request
     * @return the HTTP body of the response as byte array.
     * @throws Exception
     */
    public byte[] post(@NonNull String url, Map<String, String> headers, @NonNull String data) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = initHttpConnection(url, headers);

            connection.setRequestMethod(METHOD_POST);
            connection.setDoOutput(true);

            writeOutputStream(connection.getOutputStream(), data);

            return parseResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Synchronous GET request.
     *
     * @param url server URL
     * @return the HTTP body of the response as byte array.
     * @throws Exception
     */
    public byte[] get(@NonNull String url, Map<String, String> headers) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = initHttpConnection(url, headers);

            connection.setRequestMethod(METHOD_GET);

            return parseResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @NonNull
    private HttpURLConnection initHttpConnection(@NonNull String url, @Nullable Map<String, String> headers)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        if (connection instanceof HttpsURLConnection) {
            if (sslSocketFactory == null) {
                throw new SSLException("SSLSocketFactory failed to initialize");
            }

            ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
        }

        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        if (headers != null && headers.size() > 0) {
            final Set<Map.Entry<String, String>> entries = headers.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        return connection;
    }

    private void writeOutputStream(@NonNull OutputStream outputStream, @NonNull String data) throws IOException {
        outputStream.write(data.getBytes(Charset.forName("UTF-8")));
        outputStream.flush();
        outputStream.close();
    }

    private byte[] parseResponse(@NonNull HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        switch (responseCode) {
            case HTTP_OK:
            case HTTP_CREATED:
            case HTTP_ACCEPTED:
                return Util.convertInputStreamToByteArray(connection.getInputStream());
            case HTTP_UNAUTHORIZED:
                throw new HttpAuthenticationException(readStreamAsString(connection.getErrorStream()));
            case HTTP_FORBIDDEN:
                throw new HttpAuthorizationException(readStreamAsString(connection.getErrorStream()));
            case HTTP_INTERNAL_ERROR:
                throw new HttpServerException(readStreamAsString(connection.getErrorStream()));
            case HTTP_UNAVAILABLE:
                throw new HttpDownForMaintenanceException(readStreamAsString(connection.getErrorStream()));
            default:
                throw new UnexpectedException(readStreamAsString(connection.getErrorStream()));
        }
    }

    private String readStreamAsString(@NonNull InputStream in) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        BufferedReader connectionInputStream = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));

        String inputLine;
        while ((inputLine = connectionInputStream.readLine()) != null) {
            responseBuilder.append(inputLine);
        }
        connectionInputStream.close();

        return responseBuilder.toString();
    }

}
