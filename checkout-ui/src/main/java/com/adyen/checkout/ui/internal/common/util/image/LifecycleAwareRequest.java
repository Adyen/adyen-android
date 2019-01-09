/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 01/05/2018.
 */

package com.adyen.checkout.ui.internal.common.util.image;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

abstract class LifecycleAwareRequest extends Request implements LifecycleObserver {
    private Lifecycle mLifecycle;

    private boolean mDestroyed;

    LifecycleAwareRequest(@NonNull Rembrandt rembrandt, @NonNull RequestArgs requestArgs, @NonNull Lifecycle lifecycle) {
        super(rembrandt, requestArgs);

        mLifecycle = lifecycle;
        mLifecycle.addObserver(this);
    }

    @Override
    boolean isCancelled() {
        return mDestroyed;
    }

    @Override
    void release() {
        if (mLifecycle != null) {
            mLifecycle.removeObserver(this);
        }

        mLifecycle = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        mDestroyed = true;
        release();
    }
}
