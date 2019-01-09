/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 23/03/2018.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;

/**
 * Interface providing information about a transaction variant (e.g. the payment method GooglePay).
 */
public interface TxVariantProvider {
    /**
     * @return The transaction variant.
     */
    @NonNull
    String getTxVariant();
}
