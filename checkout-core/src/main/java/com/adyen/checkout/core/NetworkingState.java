package com.adyen.checkout.core;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 15/05/2018.
 */
public interface NetworkingState {
    /**
     * @return Whether network requests are currently being executed.
     */
    boolean isExecutingRequests();
}
