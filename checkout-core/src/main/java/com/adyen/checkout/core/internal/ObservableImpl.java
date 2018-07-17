package com.adyen.checkout.core.internal;

import android.app.Activity;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.Observable;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.internal.lifecycle.LifecycleFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified version of {@code android.arch.lifecycle.LiveData}.
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/07/2018.
 */
public class ObservableImpl<T> implements Observable<T> {
    private static final int START_VERSION = -1;

    private final Map<Observer<T>, ObserverWrapper> mObservers = new HashMap<>();

    private volatile T mData;

    private int mVersion = START_VERSION;

    private boolean mDispatchingValue;

    private boolean mDispatchInvalidated;

    private static void assertMainThread(@NonNull String methodName) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Cannot invoke " + methodName + " on a background thread");
        }
    }

    ObservableImpl(@Nullable T t) {
        if (t != null) {
            setValue(t);
        }
    }

    @Override
    public void observe(@NonNull Activity activity, @NonNull Observer<T> observer) {
        LifecycleFragment lifecycleFragment = LifecycleFragment.addIfNeeded(activity);

        if (lifecycleFragment.getState() == LifecycleFragment.State.DESTROYED) {
            return;
        }

        LifecycleBoundObserver wrapper = new LifecycleBoundObserver(lifecycleFragment, observer);
        ObserverWrapper existing = mObservers.get(observer);

        if (existing == null) {
            mObservers.put(observer, wrapper);
            lifecycleFragment.addListener(wrapper);
        }
    }

    @Override
    public void removeObserver(@NonNull final Observer<T> observer) {
        assertMainThread("removeObserver");
        ObserverWrapper removed = mObservers.remove(observer);

        if (removed == null) {
            return;
        }

        removed.detachObserver();
        removed.activeStateChanged(false);
    }

    @MainThread
    public void setValue(@NonNull T value) {
        assertMainThread("setValue");
        mVersion++;
        mData = value;
        dispatchingValue(null);
    }

    private void considerNotify(@NonNull ObserverWrapper observer) {
        if (!observer.mActive) {
            return;
        }

        if (!observer.shouldBeActive()) {
            observer.activeStateChanged(false);
            return;
        }

        if (observer.mLastVersion >= mVersion) {
            return;
        }

        observer.mLastVersion = mVersion;
        //noinspection unchecked
        observer.mObserver.onChanged((T) mData);
    }

    private void dispatchingValue(@Nullable ObserverWrapper initiator) {
        if (mDispatchingValue) {
            mDispatchInvalidated = true;
            return;
        }

        mDispatchingValue = true;

        do {
            mDispatchInvalidated = false;
            if (initiator != null) {
                considerNotify(initiator);
                initiator = null;
            } else {
                for (Map.Entry<Observer<T>, ObserverWrapper> entry : mObservers.entrySet()) {
                    considerNotify(entry.getValue());

                    if (mDispatchInvalidated) {
                        break;
                    }
                }
            }
        } while (mDispatchInvalidated);

        mDispatchingValue = false;
    }

    private final class LifecycleBoundObserver extends ObserverWrapper implements LifecycleFragment.Listener {
        private final LifecycleFragment mLifecycleFragment;

        private LifecycleBoundObserver(@NonNull LifecycleFragment lifecycleFragment, @NonNull Observer<T> observer) {
            super(observer);

            mLifecycleFragment = lifecycleFragment;
        }

        @Override
        boolean shouldBeActive() {
            return mLifecycleFragment.getState().isAtLeast(LifecycleFragment.State.STARTED);
        }

        @Override
        public void onActive() {
            activeStateChanged(true);
        }

        @Override
        public void onInactive() {
            activeStateChanged(false);
        }

        @Override
        public void onDestroy() {
            mLifecycleFragment.removeListener(this);
        }

        @Override
        void detachObserver() {
            mLifecycleFragment.removeListener(this);
        }
    }

    private abstract class ObserverWrapper {
        private final Observer<T> mObserver;

        private boolean mActive;

        private int mLastVersion = START_VERSION;

        ObserverWrapper(Observer<T> observer) {
            mObserver = observer;
        }

        abstract boolean shouldBeActive();

        void detachObserver() {
        }

        void activeStateChanged(boolean newActive) {
            if (newActive == mActive) {
                return;
            }

            mActive = newActive;

            if (mActive) {
                dispatchingValue(this);
            }
        }
    }
}
