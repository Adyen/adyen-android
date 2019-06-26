/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/4/2019.
 */

package com.adyen.checkout.base.models;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public class TestConfiguration implements Configuration {
    @NonNull
    @Override
    public Locale getShopperLocale() {
        return new Locale("en");
    }

    @NonNull
    @Override
    public Environment getEnvironment() {
        return Environment.TEST;
    }
}
