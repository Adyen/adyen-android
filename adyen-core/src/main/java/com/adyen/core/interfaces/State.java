package com.adyen.core.interfaces;

import com.adyen.core.internals.PaymentTrigger;

/**
 * State interface class.
 *
 */

public interface State {
    String TAG = State.class.getSimpleName();

    interface StateChangeListener {
        void onStateChanged(State state);
        void onStateNotChanged(State state);
    }

    State onTrigger(PaymentTrigger paymentTrigger);
}
