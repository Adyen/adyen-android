package com.adyen.core;

import android.content.Context;

import com.adyen.core.exceptions.UIModuleNotAvailableException;
import com.adyen.core.interfaces.PaymentRequestDetailsListener;
import com.adyen.core.interfaces.PaymentRequestListener;

import java.lang.reflect.InvocationTargetException;

/**
 * Factory to instantiate the default PaymentRequestListener and PaymentRequestDetailsListener.
 */
final class ListenerFactory {

    private static final String DEFAULT_PAYMENT_REQUEST_LISTENER = "com.adyen.ui.DefaultPaymentRequestListener";
    private static final String DEFAULT_PAYMENT_REQUEST_DETAILS_LISTENER = "com.adyen.ui.DefaultPaymentRequestDetailsListener";

    private static final String ERROR_MESSAGE =
            "UI module not available. Import adyen-ui or provide a PaymentRequestDetailsListener to PaymentRequest.";

    static PaymentRequestListener createAdyenPaymentRequestListener(Context context) throws UIModuleNotAvailableException {
        PaymentRequestListener listener = (PaymentRequestListener) getClassInstance(context, DEFAULT_PAYMENT_REQUEST_LISTENER);
        if (listener == null) {
            throw new UIModuleNotAvailableException(ERROR_MESSAGE);
        }
        return listener;
    }

    static PaymentRequestDetailsListener createAdyenPaymentRequestDetailsListener(Context context) throws UIModuleNotAvailableException {
        PaymentRequestDetailsListener detailsListener = (PaymentRequestDetailsListener)
                getClassInstance(context, DEFAULT_PAYMENT_REQUEST_DETAILS_LISTENER);
        if (detailsListener == null) {
            throw new UIModuleNotAvailableException(ERROR_MESSAGE);
        }
        return detailsListener;
    }

    private static Object getClassInstance(final Context context, final String clazz) {
        Object result = null;
        try {
            result = Class.forName(clazz).getConstructor(Context.class).newInstance(context);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private ListenerFactory() {
        // private constructor
    }
}
