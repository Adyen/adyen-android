package com.adyen.checkout.core.internal.lifecycle;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 10/07/2018.
 */
public final class LifecycleFragment extends Fragment {
    private static final String TAG = LifecycleFragment.class.getName();

    private final List<Listener> mListeners = new ArrayList<>();

    private State mState = State.INITIALIZED;

    @NonNull
    public static LifecycleFragment addIfNeeded(@NonNull Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();

        try {
            fragmentManager.executePendingTransactions();
        } catch (IllegalStateException e) {
            // Ignore.
        }

        Fragment lifecycleFragment = fragmentManager.findFragmentByTag(TAG);

        if (!(lifecycleFragment instanceof LifecycleFragment)) {
            lifecycleFragment = new LifecycleFragment();
            fragmentManager
                    .beginTransaction()
                    .add(lifecycleFragment, TAG)
                    .commit();
        }

        return (LifecycleFragment) lifecycleFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mState = State.CREATED;
    }

    @Override
    public void onStart() {
        super.onStart();

        mState = State.STARTED;
    }

    @Override
    public void onResume() {
        super.onResume();

        mState = State.RESUMED;

        for (Listener listener : new ArrayList<>(mListeners)) {
            listener.onActive();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mState = State.STARTED;

        for (Listener listener : new ArrayList<>(mListeners)) {
            listener.onInactive();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mState = State.CREATED;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mState = State.DESTROYED;

        for (Listener mListener : new ArrayList<>(mListeners)) {
            mListener.onDestroy();
        }
    }

    @NonNull
    public State getState() {
        return mState;
    }

    public void addListener(@NonNull Listener listener) {
        if (mListeners.contains(listener)) {
            return;
        }

        if (mState.isAtLeast(State.INITIALIZED)) {
            mListeners.add(listener);
        }

        if (mState.isAtLeast(State.RESUMED)) {
            listener.onActive();
        }
    }

    public void removeListener(@NonNull Listener listener) {
        if (!mListeners.contains(listener)) {
            return;
        }

        mListeners.remove(listener);

        if (mState.isAtLeast(State.RESUMED)) {
            listener.onInactive();
        }

        if (mState.isAtLeast(State.INITIALIZED)) {
            listener.onDestroy();
        }
    }

    public interface Listener {
        void onActive();

        void onInactive();

        void onDestroy();
    }

    public enum State {
        DESTROYED,
        INITIALIZED,
        CREATED,
        STARTED,
        RESUMED;

        public boolean isAtLeast(@NonNull State state) {
            return compareTo(state) >= 0;
        }
    }
}
