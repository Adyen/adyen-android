package com.adyen.checkout.ui.internal.wechatpay;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodFactory;
import com.adyen.checkout.util.PaymentMethodTypes;
import com.adyen.checkout.wechatpay.WeChatPayHandler;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by ran on 26/04/2018.
 */
public class WeChatPayCheckoutMethodFactory extends CheckoutMethodFactory {
    public WeChatPayCheckoutMethodFactory(@NonNull Application application) {
        super(application);
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initOneClickCheckoutMethods(@NonNull PaymentSession paymentSession) {
        return null;
    }

    @Nullable
    @Override
    public Callable<List<CheckoutMethod>> initCheckoutMethods(@NonNull PaymentSession paymentSession) {
        return new WeChatPayCallable(getApplication(), paymentSession);
    }

    private static final class WeChatPayCallable implements Callable<List<CheckoutMethod>> {
        private static final String TAG = WeChatPayCallable.class.getSimpleName();

        private static final String ERROR_MESSAGE_FORMAT = "PaymentMethod with type %s is present, but the 'checkout-wechatpay' dependency is "
                + "missing. Add the dependency to your dependency list.";

        private final Application mApplication;

        private final PaymentSession mPaymentSession;

        private WeChatPayCallable(@NonNull Application application, @NonNull PaymentSession paymentSession) {
            mApplication = application;
            mPaymentSession = paymentSession;
        }

        @Override
        public List<CheckoutMethod> call() {
            List<PaymentMethod> paymentMethods = mPaymentSession.getPaymentMethods();
            PaymentMethod weChatPayPaymentMethod = PaymentMethodImpl.findByType(paymentMethods, PaymentMethodTypes.WECHAT_PAY_SDK);

            if (weChatPayPaymentMethod != null) {
                try {
                    if (WeChatPayHandler.FACTORY.supports(mApplication, weChatPayPaymentMethod)
                            && WeChatPayHandler.FACTORY.isAvailableToShopper(mApplication, mPaymentSession, weChatPayPaymentMethod)) {
                        return Collections.<CheckoutMethod>singletonList(new WeChatPayCheckoutMethod(mApplication, weChatPayPaymentMethod));
                    }
                } catch (NoClassDefFoundError e) {
                    Log.e(TAG, String.format(ERROR_MESSAGE_FORMAT, weChatPayPaymentMethod));
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
            }

            return null;
        }
    }
}
