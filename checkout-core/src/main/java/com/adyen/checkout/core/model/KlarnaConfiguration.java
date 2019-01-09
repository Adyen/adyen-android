/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 31/10/2018.
 */

package com.adyen.checkout.core.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.internal.model.KlarnaConfigurationImpl;

@ProvidedBy(KlarnaConfigurationImpl.class)
public interface KlarnaConfiguration extends Configuration {

    /**
     * @return the URL of the service to look for the shopper details from the Social Security Number.
     */
    @NonNull
    String getShopperInfoSsnLookupUrl();
}
