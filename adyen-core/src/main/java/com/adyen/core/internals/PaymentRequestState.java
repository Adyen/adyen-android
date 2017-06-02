package com.adyen.core.internals;

import android.util.Log;

import com.adyen.core.interfaces.State;

/**
 * This enum contains individual states and state transitions.
 */

public enum PaymentRequestState implements State {
    IDLE {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                case PAYMENT_REQUESTED:
                    nextState = WAITING_FOR_PAYMENT_DATA;
                    break;
                case ERROR_OCCURRED:
                    nextState = ABORTED;
                    break;
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    WAITING_FOR_PAYMENT_DATA {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                case PAYMENT_DATA_PROVIDED:
                    nextState = FETCHING_AND_FILTERING_PAYMENT_METHODS;
                    break;
                case ERROR_OCCURRED:
                    nextState = ABORTED;
                    break;
                case PAYMENT_CANCELLED:
                    nextState = CANCELLED;
                    break;
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    FETCHING_AND_FILTERING_PAYMENT_METHODS {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                case PAYMENT_METHODS_AVAILABLE:
                    nextState = WAITING_FOR_PAYMENT_METHOD_SELECTION;
                    break;
                case ERROR_OCCURRED:
                    nextState = ABORTED;
                    break;
                case PAYMENT_CANCELLED:
                    nextState = CANCELLED;
                    break;
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    WAITING_FOR_PAYMENT_METHOD_SELECTION {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                case PAYMENT_DETAILS_REQUIRED:
                    nextState = WAITING_FOR_PAYMENT_METHOD_DETAILS;
                    break;
                case PAYMENT_DETAILS_NOT_REQUIRED:
                    nextState = PROCESSING_PAYMENT;
                    break;
                case ERROR_OCCURRED:
                    nextState = ABORTED;
                    break;
                case PAYMENT_CANCELLED:
                    nextState = CANCELLED;
                    break;
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    WAITING_FOR_PAYMENT_METHOD_DETAILS {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                case PAYMENT_DETAILS_NOT_REQUIRED:
                    nextState = PROCESSING_PAYMENT;
                    break;
                case PAYMENT_DETAILS_PROVIDED:
                    nextState = PROCESSING_PAYMENT;
                    break;
                case PAYMENT_SELECTION_CANCELLED:
                    nextState = WAITING_FOR_PAYMENT_METHOD_SELECTION;
                    break;
                case ERROR_OCCURRED:
                    nextState = ABORTED;
                    break;
                case PAYMENT_CANCELLED:
                    nextState = CANCELLED;
                    break;
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    PROCESSING_PAYMENT {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                case PAYMENT_RESULT_RECEIVED:
                    nextState = PROCESSED;
                    break;
                case REDIRECTION_REQUIRED:
                    nextState = WAITING_FOR_REDIRECTION;
                    break;
                case ERROR_OCCURRED:
                    nextState = ABORTED;
                    break;
                case PAYMENT_CANCELLED:
                    nextState = CANCELLED;
                    break;
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    WAITING_FOR_REDIRECTION {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                // The following three triggers should be handled in this state. Because after
                // opening the browser or tab, if back button is pressed and a new method is selected
                // we should handle this request.
                case PAYMENT_DETAILS_REQUIRED:
                    nextState = WAITING_FOR_PAYMENT_METHOD_DETAILS;
                    break;
                case PAYMENT_DETAILS_NOT_REQUIRED:
                    nextState = PROCESSING_PAYMENT;
                    break;
                case PAYMENT_DETAILS_PROVIDED:
                    nextState = PROCESSING_PAYMENT;
                    break;
                case RETURN_URI_RECEIVED:
                    nextState = PROCESSED;
                    break;
                case PAYMENT_SELECTION_CANCELLED:
                    nextState = WAITING_FOR_PAYMENT_METHOD_SELECTION;
                    break;
                case ERROR_OCCURRED:
                    nextState = ABORTED;
                    break;
                case PAYMENT_CANCELLED:
                    nextState = CANCELLED;
                    break;
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    PROCESSED {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                // No transition is possible from PROCESSED state.
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    ABORTED {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                // No transition is possible from ABORTED state.
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    },
    CANCELLED {
        @Override
        public State onTrigger(final PaymentTrigger paymentTrigger) {
            State nextState = this;
            switch (paymentTrigger) {
                // No transition is possible from CANCELLED state.
                default:
                    Log.d(TAG, this.toString() + " - Unknown trigger received: " + paymentTrigger.toString());
                    break;
            }
            return nextState;
        }
    }

}
