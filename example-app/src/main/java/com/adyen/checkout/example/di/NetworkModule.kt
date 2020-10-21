/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/10/2019.
 */

package com.adyen.checkout.example.di

import android.os.Build
import com.adyen.checkout.core.api.SSLSocketUtil
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.api.CheckoutApiService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.KeyStore
import java.util.Arrays
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

val networkModule = module {

    fun provideHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder().let {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            it.addNetworkInterceptor(interceptor)
        }

        if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {

            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + Arrays.toString(trustManagers)
            }

            val trustManager = trustManagers[0] as X509TrustManager
            builder.sslSocketFactory(SSLSocketUtil.TLS_SOCKET_FACTORY, trustManager)
        }

        return builder.build()
    }

    fun provideApi(httpClient: OkHttpClient): CheckoutApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.MERCHANT_SERVER_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()
            ).build()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(CheckoutApiService::class.java)
    }

    single { provideHttpClient() }
    single { provideApi(get()) }
}
