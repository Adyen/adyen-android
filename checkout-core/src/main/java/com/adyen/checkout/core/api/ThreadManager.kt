/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.core.api;

import android.os.Handler;
import android.os.Looper;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadManager {

    public static final Handler MAIN_HANDLER = getMainHandler();
    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static Handler getMainHandler() {
        try {
            return new Handler(Looper.getMainLooper());
        } catch (RuntimeException e) {
            // avoid Looper class on testing
            return new Handler();
        }
    }

    private ThreadManager() {
        throw new NoConstructorException();
    }
}
