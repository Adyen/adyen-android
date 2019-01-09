/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/07/2018.
 */

package com.adyen.checkout.core;

import android.app.Activity;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

/**
 * Interface providing means to subscribe to changes of an object.
 */
public interface Observable<T> {
    /**
     * Adds the given {@link Observer} within the {@link Activity Activity's} lifecycle to the list of {@link Observer Observers} to be notified when
     * this {@link Observable} changes.
     *
     * @param activity The current {@link Activity} in which the
     * @param observer The {@link Observer} to be notified about changes.
     */
    @MainThread
    void observe(@NonNull Activity activity, @NonNull Observer<T> observer);

    /**
     * Removed the given {@link Observer}.
     *
     * @param observer The {@link Observer} to be removed.
     */
    @MainThread
    void removeObserver(@NonNull Observer<T> observer);
}
