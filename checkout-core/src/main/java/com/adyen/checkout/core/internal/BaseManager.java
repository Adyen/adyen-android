/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 13/07/2018.
 */

package com.adyen.checkout.core.internal;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.internal.lifecycle.LifecycleFragment;

import java.util.ArrayList;
import java.util.List;

abstract class BaseManager<H, D> {
    private final List<H> mHandlers = new ArrayList<>();

    private final Listener mListener;

    private D mData;

    BaseManager(@NonNull Listener listener) {
        mListener = listener;
    }

    void addHandler(@NonNull Activity activity, @NonNull H handler) {
        new ActivityScopedHandler(activity, handler);
        checkDispatch();
    }

    void setData(@NonNull D data) {
        mData = data;
        checkDispatch();
    }

    abstract void dispatch(@NonNull H handler, @NonNull D data);

    private void checkDispatch() {
        if (mData != null) {
            if (!mHandlers.isEmpty()) {
                H handler = mHandlers.get(0);
                dispatch(handler, mData);
                mListener.onHandled();
                mData = null;
            }
        }
    }

    interface Listener {
        void onHandled();
    }

    private final class ActivityScopedHandler implements LifecycleFragment.Listener {
        private LifecycleFragment mLifecycleFragment;

        private H mHandler;

        private ActivityScopedHandler(@NonNull Activity activity, @NonNull H handler) {
            mLifecycleFragment = LifecycleFragment.addIfNeeded(activity);
            mHandler = handler;
            mLifecycleFragment.addListener(this);
        }

        @Override
        public void onActive() {
            mHandlers.add(0, mHandler);
            checkDispatch();
        }

        @Override
        public void onInactive() {
            mHandlers.remove(mHandler);
        }

        @Override
        public void onDestroy() {
            mLifecycleFragment.removeListener(this);
            mHandler = null;
            mLifecycleFragment = null;
        }
    }
}
