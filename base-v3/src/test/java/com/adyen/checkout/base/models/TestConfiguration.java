/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/4/2019.
 */

package com.adyen.checkout.base.models;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public class TestConfiguration extends Configuration {

    public TestConfiguration() {
        super(Locale.US, Environment.TEST, null);
    }

    protected TestConfiguration(@NonNull Parcel in) {
        super(in);
    }
}
