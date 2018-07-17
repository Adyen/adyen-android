package com.adyen.checkout.base;

import android.support.annotation.NonNull;

/**
 * Interface providing information about a host.
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 31/05/2018.
 */
public interface HostProvider {
    /**
     * @return The host's URL.
     */
    @NonNull
    String getUrl();
}
