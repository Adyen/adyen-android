/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 28/11/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

public interface GiroPayIssuer extends Parcelable {
    /**
     * @return The name of the bank.
     */
    @NonNull
    String getBankName();

    /**
     * @return The Bank Identifier Code of the bank in ISO 9362 format.
     */
    @NonNull
    String getBic();

    /**
     * @return The Bank Identifier Code of the bank.
     */
    @NonNull
    String getBlz();
}
