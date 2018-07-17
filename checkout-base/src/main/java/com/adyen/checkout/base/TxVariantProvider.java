package com.adyen.checkout.base;

import android.support.annotation.NonNull;

/**
 * Interface providing information about a transaction variant (e.g. the payment method GooglePay).
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 23/03/2018.
 */
public interface TxVariantProvider {
    /**
     * @return The transaction variant.
     */
    @NonNull
    String getTxVariant();
}
