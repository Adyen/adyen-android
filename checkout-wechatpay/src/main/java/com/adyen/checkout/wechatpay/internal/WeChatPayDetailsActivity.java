/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 26/04/2018.
 */

package com.adyen.checkout.wechatpay.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.AdditionalDetails;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.PaymentResult;
import com.adyen.checkout.core.handler.AdditionalDetailsHandler;
import com.adyen.checkout.core.handler.ErrorHandler;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.WeChatPayDetails;
import com.adyen.checkout.core.model.WeChatPaySdkRedirectData;
import com.adyen.checkout.util.PaymentMethodTypes;
import com.adyen.checkout.wechatpay.WeChatPayHandler;
import com.tencent.mm.opensdk.modelbase.BaseResp;

public class WeChatPayDetailsActivity extends Activity implements WeChatPayListener {
    private static final String EXTRA_PAYMENT_REFERENCE = "EXTRA_PAYMENT_REFERENCE";

    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private PaymentReference mPaymentReference;

    private PaymentHandler mPaymentHandler;

    private PaymentMethod mPaymentMethod;

    private WeChatPayUtil mWeChatPayUtil;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, WeChatPayDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mPaymentReference = intent.getParcelableExtra(EXTRA_PAYMENT_REFERENCE);
        mPaymentMethod = intent.getParcelableExtra(EXTRA_PAYMENT_METHOD);

        if (!PaymentMethodTypes.WECHAT_PAY_SDK.equals(mPaymentMethod.getType())) {
            throw new RuntimeException("Invalid PaymentMethod: " + mPaymentMethod);
        }

        mPaymentHandler = mPaymentReference.getPaymentHandler(this);
        mPaymentHandler.setAdditionalDetailsHandler(this, new AdditionalDetailsHandler() {
            @Override
            public void onAdditionalDetailsRequired(@NonNull AdditionalDetails additionalDetails) {
                try {
                    WeChatPaySdkRedirectData redirectData = additionalDetails.getRedirectData(WeChatPaySdkRedirectData.class);

                    if (!mWeChatPayUtil.initiateWeChatPayRedirect(redirectData)) {
                        CheckoutException checkoutException = new CheckoutException.Builder("Could not redirect to WeChat app.", null).build();
                        Intent resultData = new Intent();
                        resultData.putExtra(WeChatPayHandler.RESULT_CHECKOUT_EXCEPTION, checkoutException);
                        setResult(WeChatPayHandler.RESULT_CODE_ERROR, resultData);
                        finish();
                    }
                } catch (CheckoutException e) {
                    Intent resultData = new Intent();
                    resultData.putExtra(WeChatPayHandler.RESULT_CHECKOUT_EXCEPTION, e);
                    setResult(WeChatPayHandler.RESULT_CODE_ERROR, resultData);
                    finish();
                }
            }
        });
        mPaymentHandler.setErrorHandler(this, new ErrorHandler() {
            @Override
            public void onError(@NonNull CheckoutException error) {
                Intent resultData = new Intent();
                resultData.putExtra(WeChatPayHandler.RESULT_CHECKOUT_EXCEPTION, error);
                setResult(WeChatPayHandler.RESULT_CODE_ERROR, resultData);
                finish();
            }
        });
        mPaymentHandler.getPaymentResultObservable().observe(this, new Observer<PaymentResult>() {
            @Override
            public void onChanged(@NonNull PaymentResult paymentResult) {
                Intent resultData = new Intent();
                resultData.putExtra(WeChatPayHandler.RESULT_PAYMENT_RESULT, paymentResult);
                setResult(WeChatPayHandler.RESULT_CODE_OK, resultData);
                finish();
            }
        });
        WeChatPayProvider weChatPayProvider = new WeChatPayProvider.Builder()
                .callbackActivity(WeChatPayDetailsActivity.class)
                .build();
        mWeChatPayUtil = WeChatPayUtil.get(getApplication(), weChatPayProvider, WeChatPayDetailsActivity.this);

        if (savedInstanceState == null) {
            mPaymentHandler.initiatePayment(mPaymentMethod, null);
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        mWeChatPayUtil.handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWeChatPayUtil.detach();
        mWeChatPayUtil = null;
    }

    @Override
    public void onPaymentDetails(@NonNull BaseResp baseResp, @NonNull WeChatPayDetails weChatPayDetails) {
        mPaymentHandler.submitAdditionalDetails(weChatPayDetails);
    }
}
