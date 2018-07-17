package com.adyen.checkout.ui.internal.common.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 29/03/2018.
 */
public final class Operation<I, O> {
    private final I mInput;

    private final O mOutput;

    private final boolean mRunning;

    private final Throwable mError;

    @NonNull
    public static <I, O> Operation<I, O> running(@NonNull I input) {
        return new Operation<>(input, null, true, null);
    }

    @NonNull
    public static <I, O> Operation<I, O> complete(@NonNull I input, @NonNull O output) {
        return new Operation<>(input, output, false, null);
    }

    @NonNull
    public static <I, O> Operation<I, O> error(@NonNull I input, @Nullable O output, @NonNull Throwable error) {
        return new Operation<>(input, output, false, error);
    }

    private Operation(@NonNull I input, @Nullable O output, boolean running, @Nullable Throwable error) {
        mInput = input;
        mOutput = output;
        mRunning = running;
        mError = error;
    }

    public void dispatchCurrentState(@NonNull Listener<I, O> listener) {
        if (mRunning) {
            listener.onRunning(mInput);
        } else if (mOutput != null && mError == null) {
            listener.onComplete(mInput, mOutput);
        } else if (mError != null) {
            listener.onError(mInput, mOutput, mError);
        } else {
            throw new IllegalStateException("Operation is in unknown state.");
        }
    }

    @NonNull
    public I getInput() {
        return mInput;
    }

    @Nullable
    public O getOutput() {
        return mOutput;
    }

    public boolean isRunning() {
        return mRunning;
    }

    @Nullable
    public Throwable getError() {
        return mError;
    }

    public interface Listener<I, O> {
        void onRunning(@NonNull I i);

        void onComplete(@NonNull I i, @NonNull O o);

        void onError(@NonNull I i, @Nullable O o, @NonNull Throwable error);
    }

    public abstract static class SimpleListener<I, O> implements Listener<I, O> {
        @Override
        public void onRunning(@NonNull I i) {
            // Subclasses may override.
        }

        @Override
        public void onComplete(@NonNull I i, @NonNull O o) {
            // Subclasses may override.
        }

        @Override
        public void onError(@NonNull I i, @Nullable O o, @NonNull Throwable error) {
            // Subclasses may override.
        }
    }
}
