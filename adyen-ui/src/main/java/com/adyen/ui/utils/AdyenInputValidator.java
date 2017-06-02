package com.adyen.ui.utils;

import android.os.Handler;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AdyenInputValidator {

    private HashMap<View, Boolean> inputFields = new HashMap<>();

    private boolean allInputReady = false;

    private OnReadyStateChangedListener onReadyStateChangedListener;

    public void setReady(View view, boolean ready) {
        if (inputFields.containsKey(view) && inputFields.get(view) != ready) {
            inputFields.put(view, ready);
            onReadyStateMaybeChanged();
        }
    }

    public void addInputField(View newView) {
        inputFields.put(newView, false);
    }

    private boolean isAllInputReady() {
        boolean ready = true;
        final Set<Map.Entry<View, Boolean>> entries = inputFields.entrySet();
        for (Map.Entry<View, Boolean> entry : entries) {
            ready &= entry.getValue();
        }
        return ready;
    }

    private void onReadyStateMaybeChanged() {
        boolean oldState = allInputReady;
        boolean newState = isAllInputReady();
        if (oldState != newState) {
            allInputReady = newState;
            onReadyStateChanged(newState);
        }
    }

    private void onReadyStateChanged(final boolean newState) {
        if (onReadyStateChangedListener != null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    onReadyStateChangedListener.onReadyStateChanged(newState);
                }
            });
        }
    }

    public void setOnReadyStateChangedListener(OnReadyStateChangedListener listener) {
        this.onReadyStateChangedListener = listener;
    }

    public interface OnReadyStateChangedListener {
        void onReadyStateChanged(boolean isReady);
    }
}
