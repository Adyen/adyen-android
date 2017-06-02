package com.adyen.core.internals;

import android.util.Log;

import com.adyen.core.interfaces.State;

/**
 * Payment Processor State Machine.
 */

public class PaymentProcessorStateMachine implements State, State.StateChangeListener {

    private static final String TAG = PaymentProcessorStateMachine.class.getSimpleName();
    private State paymentProcessorState;
    private StateChangeListener stateChangeListener;

    public PaymentProcessorStateMachine(final StateChangeListener stateChangeListener) {
        Log.d(TAG, "PaymentProcessorStateMachine() constructed");
        this.paymentProcessorState = PaymentRequestState.IDLE;
        this.stateChangeListener = stateChangeListener;
    }

    @Override
    public void onStateChanged(final State state) {
        paymentProcessorState = state;
        stateChangeListener.onStateChanged(paymentProcessorState);
    }

    @Override
    public void onStateNotChanged(final State state) {
        stateChangeListener.onStateNotChanged(paymentProcessorState);
    }

    @Override
    public State onTrigger(final PaymentTrigger paymentTrigger) {
        final State newState = this.paymentProcessorState.onTrigger(paymentTrigger);
        if (paymentProcessorState != newState) {
            paymentProcessorState = newState;
            onStateChanged(newState);
        } else {
            onStateNotChanged(paymentProcessorState);
        }
        return newState;
    }

}
