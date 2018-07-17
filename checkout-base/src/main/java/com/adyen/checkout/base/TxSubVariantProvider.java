package com.adyen.checkout.base;

import android.support.annotation.NonNull;

/**
 * Interface providing information about a transaction sub variant, for example a single issuer of an issuer based payment method (e.g. an iDEAL
 * issuer such as ING).
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 23/03/2018.
 */
public interface TxSubVariantProvider {
    /**
     * @return The sub variant of the transaction variant.
     */
    @NonNull
    String getTxSubVariant();
}
