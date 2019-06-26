/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */

package com.adyen.checkout.card.model;

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
