/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 31/05/2018.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;

/**
 * Interface providing information about a host.
 */
public interface HostProvider {
    /**
     * @return The host's URL.
     */
    @NonNull
    String getUrl();
}
