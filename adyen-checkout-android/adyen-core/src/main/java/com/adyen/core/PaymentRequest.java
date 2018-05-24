package com.adyen.core;

import android.content.Context;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.adyen.core.interfaces.DeletePreferredPaymentMethodListener;
import com.adyen.core.interfaces.PaymentRequestDetailsListener;
import com.adyen.core.interfaces.PaymentRequestListener;
import com.adyen.core.internals.PaymentTrigger;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;

import static com.adyen.core.constants.Constants.PaymentRequest.PAYMENT_REQUEST_CANCELLED_INTENT;

/**
 * Provides means to initiate a payment request.
 *
 */
public class PaymentRequest {

    private static final String TAG = PaymentRequest.class.getSimpleName();

    private Context context;

    private PaymentStateHandler paymentStateHandler;

    /**
     * Constructs the payment request for the general flow with default UI.
     * @param paymentRequestListener
     */
    public PaymentRequest(@NonNull final Context context,
                          @NonNull final PaymentRequestListener paymentRequestListener) {
        this(context, paymentRequestListener, null);
    }

    /**
     * Constructs the payment request for the advanced flow with your custom UI.
     * @param paymentRequestListener Listener that is notified for onPaymentDataRequested and onPaymentResult.
     * @param paymentRequestDetailsListener Advanced listener that is needed to use custom UI.
     */
    public PaymentRequest(@NonNull final Context context, @NonNull final PaymentRequestListener paymentRequestListener,
                          @NonNull final PaymentRequestDetailsListener paymentRequestDetailsListener) {
        this.context = context;
        this.paymentStateHandler = new PaymentStateHandler(context, this, paymentRequestListener,
                paymentRequestDetailsListener);

        LocalBroadcastManager.getInstance(this.context).registerReceiver(
                this.paymentStateHandler.getPaymentBroadcastReceivers().getPaymentRequestCancellationReceiver(),
                new IntentFilter(PAYMENT_REQUEST_CANCELLED_INTENT));
    }

    /**
     * Starts the payment request.
     */
    public void start() {
        if (paymentStateHandler.hasPaymentRequestDetailsListener()) {
            paymentStateHandler.getPaymentProcessorStateMachine().onTrigger(PaymentTrigger.PAYMENT_REQUESTED);
        } else {
            paymentStateHandler.getPaymentProcessorStateMachine().onTrigger(PaymentTrigger.ERROR_OCCURRED);
        }
    }

    /**
     * Cancels the payment request.
     */
    public void cancel() {
        paymentStateHandler.getPaymentProcessorStateMachine().onTrigger(PaymentTrigger.PAYMENT_CANCELLED);
    }

    /**
     * Returns the selected payment method.
     * If this method is called prior to the selection of a payment method, it will return null.
     * @return Payment method if already selected, otherwise null.
     */
    @Nullable
    public PaymentMethod getPaymentMethod() {
        return paymentStateHandler.getPaymentMethod();
    }

    /**
     * Returns the public key that is needed to encrypt data.
     * @return public key as a string, if available.
     */
    @Nullable
    public String getPublicKey() {
        return paymentStateHandler.getPublicKey();
    }

    /**
     * Returns the generation time of the request.
     * @return the generation time, if available.
     */
    @Nullable
    public String getGenerationTime() {
        return paymentStateHandler.getGenerationTime();
    }

    /**
     * Get the payment {@link Amount}.
     * If called before amount information is received, it will return null.
     * @return payment amount
     */
    @Nullable
    public Amount getAmount() {
        return paymentStateHandler.getAmount();
    }

    /**
     * Get the ShopperReference.
     * @return The shopper reference, if available.
     */
    public String getShopperReference() {
        return paymentStateHandler.getShopperReference();
    }

    /**
     * Returns the PaymentRequestListener that the PaymentRequest was initiated with.
     * @return The PaymentRequestListener
     */
    public PaymentRequestListener getPaymentRequestListener() {
        return paymentStateHandler.getPaymentRequestListener();
    }

    PaymentStateHandler getPaymentStateHandler() {
        return paymentStateHandler;
    }

    /**
     * Delete a preferred {@link PaymentMethod}.
     * This will remove the stored payment details from this shopper on the back-end.
     * @param paymentMethod A reference to the {@link PaymentMethod} that should be removed.
     *                      Must be a preferred {@link PaymentMethod}.
     * @param listener {@link DeletePreferredPaymentMethodListener} listener to get the result.
     */
    public void deletePreferredPaymentMethod(final PaymentMethod paymentMethod, DeletePreferredPaymentMethodListener listener) {
        paymentStateHandler.deletePreferredPaymentMethod(paymentMethod, listener);
    }

}
