/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */

package com.adyen.checkout.example.api

import android.os.Build
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.payments.request.CardPaymentMethod
import com.adyen.checkout.base.model.payments.request.DotpayPaymentMethod
import com.adyen.checkout.base.model.payments.request.EPSPaymentMethod
import com.adyen.checkout.base.model.payments.request.EntercashPaymentMethod
import com.adyen.checkout.base.model.payments.request.GenericPaymentMethod
import com.adyen.checkout.base.model.payments.request.IdealPaymentMethod
import com.adyen.checkout.base.model.payments.request.MolpayPaymentMethod
import com.adyen.checkout.base.model.payments.request.OpenBankingPaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.model.payments.response.QrCodeAction
import com.adyen.checkout.base.model.payments.response.RedirectAction
import com.adyen.checkout.base.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.base.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.base.model.payments.response.VoucherAction
import com.adyen.checkout.core.api.SSLSocketUtil
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.api.model.PaymentMethodsRequest
import com.adyen.checkout.example.model.PaymentsApiResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.security.KeyStore
import java.util.* // ktlint-disable no-wildcard-imports
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

interface CheckoutApiService {

    companion object {
        val INSTANCE: CheckoutApiService by lazy {

            // Add custom adapters for classes that are not properly mapped
            val moshi = Moshi.Builder()
                    .add(PolymorphicJsonAdapterFactory.of(PaymentMethodDetails::class.java, PaymentMethodDetails.TYPE)
                            .withSubtype(CardPaymentMethod::class.java, CardPaymentMethod.PAYMENT_METHOD_TYPE)
                            .withSubtype(IdealPaymentMethod::class.java, IdealPaymentMethod.PAYMENT_METHOD_TYPE)
                            .withSubtype(MolpayPaymentMethod::class.java, MolpayPaymentMethod.PAYMENT_METHOD_TYPE)
                            .withSubtype(EPSPaymentMethod::class.java, EPSPaymentMethod.PAYMENT_METHOD_TYPE)
                            .withSubtype(DotpayPaymentMethod::class.java, DotpayPaymentMethod.PAYMENT_METHOD_TYPE)
                            .withSubtype(EntercashPaymentMethod::class.java, EntercashPaymentMethod.PAYMENT_METHOD_TYPE)
                            .withSubtype(OpenBankingPaymentMethod::class.java, OpenBankingPaymentMethod.PAYMENT_METHOD_TYPE)
                            .withSubtype(GenericPaymentMethod::class.java, "other")
                    )
                    .add(PolymorphicJsonAdapterFactory.of(Action::class.java, Action.TYPE)
                            .withSubtype(RedirectAction::class.java, RedirectAction.ACTION_TYPE)
                            .withSubtype(Threeds2FingerprintAction::class.java, Threeds2FingerprintAction.ACTION_TYPE)
                            .withSubtype(Threeds2ChallengeAction::class.java, Threeds2ChallengeAction.ACTION_TYPE)
                            .withSubtype(QrCodeAction::class.java, QrCodeAction.ACTION_TYPE)
                            .withSubtype(VoucherAction::class.java, VoucherAction.ACTION_TYPE)
                    )
                    .build()
            val converter = MoshiConverterFactory.create(moshi)

            Retrofit.Builder()
                    .baseUrl(BuildConfig.MERCHANT_SERVER_URL)
                    .client(Util.enableTls12OnPreLollipop(OkHttpClient.Builder()).build())
                    .addConverterFactory(converter)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .build()
                    .create(CheckoutApiService::class.java)
        }
    }

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("paymentMethods")
    fun paymentMethodsAsync(@Body paymentMethodsRequest: PaymentMethodsRequest): Deferred<Response<PaymentMethodsApiResponse>>

    // There is no native support for JSONObject in either Moshi or Gson, so using RequestBody as a work around for now
    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("payments")
    fun payments(@Body paymentsRequest: RequestBody): Call<PaymentsApiResponse>

    @Headers(BuildConfig.API_KEY_HEADER_NAME + ":" + BuildConfig.CHECKOUT_API_KEY)
    @POST("payments/details")
    fun details(@Body detailsRequest: RequestBody): Call<PaymentsApiResponse>

    class Util private constructor() {

        init {
            throw IllegalStateException("No instances.")
        }

        companion object {
            internal fun enableTls12OnPreLollipop(client: OkHttpClient.Builder): OkHttpClient.Builder {
                if (Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {

                    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                    trustManagerFactory.init(null as KeyStore?)
                    val trustManagers = trustManagerFactory.trustManagers
                    if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                        throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
                    }

                    val trustManager = trustManagers[0] as X509TrustManager
                    client.sslSocketFactory(SSLSocketUtil.TLS_SOCKET_FACTORY, trustManager)
                }

                return client
            }
        }
    }
}
