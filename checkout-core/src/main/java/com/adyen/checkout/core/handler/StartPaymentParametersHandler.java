package com.adyen.checkout.core.handler;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.StartPaymentParameters;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 14/05/2018.
 */
public interface StartPaymentParametersHandler extends ErrorHandler {
    /**
     * Called when the payment has been initialized with the response from the Adyen payments platform.
     *
     * @param startPaymentParameters The {@link StartPaymentParameters}.
     */
    void onPaymentInitialized(@NonNull StartPaymentParameters startPaymentParameters);
}
