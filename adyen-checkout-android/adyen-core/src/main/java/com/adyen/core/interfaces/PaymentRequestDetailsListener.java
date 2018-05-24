package com.adyen.core.interfaces;

import android.support.annotation.NonNull;

import com.adyen.core.PaymentRequest;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.core.models.paymentdetails.PaymentDetails;

import java.util.Collection;
import java.util.List;

/**
 * Listener for listening to detailed events from {@link PaymentRequest}.
 * This listener has to be implemented for advanced flow in addition to {@link PaymentRequestListener}.
 *
 * If the UI is required to be handled by the client application, this listener has to be implemented
 * by the client.
 *
 */
public interface PaymentRequestDetailsListener {

    /**
     * A list of payment methods is presented to the client.
     * A call to {@link PaymentMethodCallback#completionWithPaymentMethod(PaymentMethod)} is expected.
     * The selected payment method is supposed to be notified via the callback.
     * This method is required if the client application is controlling the UI for presenting the payment methods.
     *
     * @param paymentRequest {@link PaymentRequest} instance for which the payment methods are presented.
     * @param preferredPaymentMethods A list of preferred {@link PaymentMethod} items.
     * @param availablePaymentMethods A list of available {@link PaymentMethod} items.
     * @param callback {@link PaymentMethodCallback} instance for notifying {@link PaymentRequest} for the
     *                                              method selection.
     */
    void onPaymentMethodSelectionRequired(@NonNull PaymentRequest paymentRequest,
                                          @NonNull List<PaymentMethod> preferredPaymentMethods,
                                          @NonNull List<PaymentMethod> availablePaymentMethods,
                                          @NonNull PaymentMethodCallback callback);

    /**
     * Requires a client to call the redirect URL and provide the return URI.
     * If the selected payment requires a redirect, then redirect URL is sent to the client. The client
     * is supposed to open this redirect page. When the redirected page is done with the payment, it will
     * call the deep-link URL of the app. Then the application should notify the return URL to the SDK via
     * the {@link UriCallback}.
     *
     * @param paymentRequest {@link PaymentRequest} reference.
     * @param redirectUrl Redirect URL in a string format.
     * @param uriCallback The callback for the client to provide the return URI.
     */
    void onRedirectRequired(@NonNull PaymentRequest paymentRequest, @NonNull String redirectUrl,
                            @NonNull UriCallback uriCallback);

    /**
     * Client is asked to provide the payment details for the selected payment method.
     * This callback will only be triggered if the selected payment method requires additional details.
     *
     * @param paymentRequest {@link PaymentRequest} reference.
     * @param inputDetails The collection of {@link InputDetail} that have to be provided by the shopper.
     * @param callback {@link PaymentDetailsCallback} instance in order to inform {@link PaymentRequest}
     *                                               about the required fields.
     *                                               {@link PaymentDetailsCallback} needs to be called with an instance of {@link PaymentDetails} that
     *                                               is to be created using the {@link InputDetail} collection.
     */
    void onPaymentDetailsRequired(@NonNull PaymentRequest paymentRequest, @NonNull Collection<InputDetail> inputDetails,
                                  @NonNull PaymentDetailsCallback callback);

}
