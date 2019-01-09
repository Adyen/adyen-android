/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 15/05/2018.
 */

package com.adyen.checkout.core;

public interface NetworkingState {
    /**
     * @return Whether network requests are currently being executed.
     */
    boolean isExecutingRequests();
}
