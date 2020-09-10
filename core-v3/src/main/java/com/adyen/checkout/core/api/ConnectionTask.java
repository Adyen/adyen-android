/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/3/2019.
 */

package com.adyen.checkout.core.api;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@link FutureTask} that wraps a {@link Connection} callable.
 *
 * @param <T> The type returned by the {@link Connection}
 */
public abstract class ConnectionTask<T> extends FutureTask<T> {
    private static final String TAG = LogUtil.getTag();

    private final long mTimeOut;

    /**
     * A cancellable task that runs a {@link Connection}.
     *
     * @param connection The Connection to be ran.
     */
    protected ConnectionTask(@NonNull Connection<T> connection) {
        // don't hold a reference to the connection, the FutureTask handles it's lifecycle
        this(connection, 0);
    }

    /**
     * A cancellable task that runs a {@link Connection}.
     *
     * @param connection The Connection to be ran.
     * @param timeOut A time out in milliseconds to cancel the connection.
     */
    @SuppressWarnings(Lint.WEAKER_ACCESS)
    protected ConnectionTask(@NonNull Connection<T> connection, long timeOut) {
        super(connection);
        mTimeOut = timeOut;
    }

    @CallSuper
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        Logger.d(TAG, "cancel - " + mayInterruptIfRunning);
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public void run() {
        if (mTimeOut > 0) {
            Logger.d(TAG, "run with timeout - " + mTimeOut);
        }
        super.run();
        if (mTimeOut > 0) {
            try {
                get(mTimeOut, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                Logger.d(TAG, "ExecutionException", e);
            } catch (InterruptedException e) {
                Logger.d(TAG, "InterruptedException", e);
            } catch (TimeoutException e) {
                Logger.e(TAG, "Task timed out after " + mTimeOut + " milliseconds.");
                cancel(true);
            }
        }
    }
}
