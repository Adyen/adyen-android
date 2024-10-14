/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/10/2019.
 */

package com.adyen.checkout.example.di

import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.api.CheckoutApiService
import com.adyen.checkout.example.data.api.adapter.JSONObjectAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    internal fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        val authorizationHeader = (BuildConfig.AUTHORIZATION_HEADER_NAME to BuildConfig.AUTHORIZATION_HEADER_VALUE)
            .takeIf { it.first.isNotBlank() }

        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
                if (authorizationHeader != null) redactHeader(authorizationHeader.first)
            }
            builder.addNetworkInterceptor(interceptor)
        }

        if (authorizationHeader != null) {
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(authorizationHeader.first, authorizationHeader.second)
                    .build()

                chain.proceed(request)
            }
        }

        return builder.build()
    }

    @Singleton
    @Provides
    internal fun provideConverterFactory(): Converter.Factory = MoshiConverterFactory.create(
        Moshi.Builder()
            .add(JSONObjectAdapter())
            .add(KotlinJsonAdapterFactory())
            .build(),
    )

    @Singleton
    @Provides
    @Named("RetrofitCheckout")
    internal fun provideRetrofit(
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory,
        @BaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()

    @Provides
    internal fun provideApiService(@Named("RetrofitCheckout") retrofit: Retrofit): CheckoutApiService =
        retrofit.create(CheckoutApiService::class.java)

    @Module
    @InstallIn(SingletonComponent::class)
    object BaseUrlModule {

        @BaseUrl
        @Provides
        fun provideBaseUrl(): String = BuildConfig.MERCHANT_SERVER_URL
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl
