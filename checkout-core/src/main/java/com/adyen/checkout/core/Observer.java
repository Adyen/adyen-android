package com.adyen.checkout.core;

import android.support.annotation.NonNull;

/**
 * Interface to receive updates for a subscription to an {@link Observable}.
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/07/2018.
 */
public interface Observer<T> {
    /**
     * Called when the data has changed.
     *
     * @param t The new data.
     */
    void onChanged(@NonNull T t);
}
