/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 30/01/2018.
 */

package com.adyen.example;

import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.adyen.example.model.PaymentSetupRequest;
import com.adyen.example.model.PaymentVerifyRequest;
import com.adyen.example.model.PaymentVerifyResponse;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CheckoutService {
    CheckoutService INSTANCE = new Retrofit.Builder()
            .baseUrl(BuildConfig.MERCHANT_SERVER_URL)
            .client(
                    Util.enableTls12OnPreLollipop(new OkHttpClient.Builder()
                            .addNetworkInterceptor(
                                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                            ))
                            .build()
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(CheckoutService.class);

    @NonNull
    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("paymentSession")
    Observable<ResponseBody> paymentSession(@NonNull @Body PaymentSetupRequest paymentSetupRequest);

    @NonNull
    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("payments/result")
    Observable<PaymentVerifyResponse> verify(@NonNull @Body PaymentVerifyRequest paymentVerifyRequest);

    final class Util {
        @NonNull
        static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
            if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                try {
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init((KeyStore) null);
                    TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                    if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                        throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
                    }

                    X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                    SSLContext sslContext = SSLContext.getInstance(TlsSocketFactory.TLS_V12);
                    sslContext.init(null, trustManagers, new SecureRandom());
                    client.sslSocketFactory(new TlsSocketFactory(sslContext.getSocketFactory()), trustManager);
                } catch (Exception exc) {
                    Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
                }
            }

            return client;
        }

        private Util() {
            throw new IllegalStateException("No instances.");
        }
    }
}
