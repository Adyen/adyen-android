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
import com.adyen.checkout.example.data.api.RecurringApiService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import java.util.*
import javax.inject.Singleton
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val BASE_URL = if (CheckoutApiService.isRealUrlAvailable())
        BuildConfig.MERCHANT_SERVER_URL
    else
        "http://myserver.com/my/endpoint/"

    private val BASE_URL_RECURRING = if (RecurringApiService.isRealUrlAvailable())
        BuildConfig.MERCHANT_RECURRING_SERVER_URL
    else
        "http://myserver.com/my/endpoint/"

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addNetworkInterceptor(interceptor)
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

    @Singleton
    @Provides
    fun provideConverterFactory(): Converter.Factory = MoshiConverterFactory.create(
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    )

    @Singleton
    @Provides
    @Named("RetrofitCheckout")
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

    @Singleton
    @Provides
    @Named("RetrofitRecurring")
    fun provideRetrofitRecurring(
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_RECURRING)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

    @Provides
    fun provideApiService(@Named("RetrofitCheckout") retrofit: Retrofit): CheckoutApiService =
        retrofit.create(CheckoutApiService::class.java)

    @Provides
    fun provideRecurringApiService(@Named("RetrofitRecurring") retrofit: Retrofit): RecurringApiService =
        retrofit.create(RecurringApiService::class.java)
}
