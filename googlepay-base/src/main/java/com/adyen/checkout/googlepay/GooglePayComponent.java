/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

package com.adyen.checkout.googlepay;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.GooglePayPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.ComponentException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.googlepay.util.GooglePayUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;

public class GooglePayComponent extends
        BasePaymentComponent<GooglePayConfiguration, GooglePayInputData, GooglePayOutputData, GooglePayComponentState> {
    private static final String TAG = LogUtil.getTag();

    public static final PaymentComponentProvider<GooglePayComponent, GooglePayConfiguration> PROVIDER = new GooglePayProvider();

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.GOOGLE_PAY};

    public GooglePayComponent(@NonNull PaymentMethod paymentMethod, @NonNull GooglePayConfiguration configuration) {
        super(paymentMethod, configuration);
    }

    @NonNull
    @Override
    protected GooglePayOutputData onInputDataChanged(@NonNull GooglePayInputData inputData) {
        return new GooglePayOutputData(inputData.getPaymentData());
    }

    @NonNull
    @Override
    protected GooglePayComponentState createComponentState() {
        final PaymentData paymentData = getOutputData() != null ? getOutputData().getPaymentData() : null;

        final PaymentComponentData<GooglePayPaymentMethod> paymentComponentData = new PaymentComponentData<>();
        final GooglePayPaymentMethod paymentMethod = GooglePayUtils.createGooglePayPaymentMethod(paymentData);
        paymentComponentData.setPaymentMethod(paymentMethod);
        return new GooglePayComponentState(paymentComponentData, getOutputData().isValid(), getOutputData().getPaymentData());
    }

    @NonNull
    @Override
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }

    /**
     * Start the GooglePay screen which will return the result to the provided Activity.
     *
     * @param activity    The activity to start the screen and later receive the result.
     * @param requestCode The code that will be returned on the {@link Activity#onActivityResult(int, int, Intent)}
     */
    @SuppressWarnings("JavadocReference")
    public void startGooglePayScreen(@NonNull Activity activity, int requestCode) {
        Logger.d(TAG, "startGooglePayScreen");
        final PaymentsClient paymentsClient = Wallet.getPaymentsClient(activity, GooglePayUtils.createWalletOptions(getConfiguration()));
        final PaymentDataRequest paymentDataRequest = GooglePayUtils.createPaymentDataRequest(getConfiguration());
        AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(paymentDataRequest), activity, requestCode);

    }

    /**
     * Handle the result from the GooglePay screen that was started by {@link #startGooglePayScreen(Activity, int)}.
     *
     * @param resultCode The result code from the {@link Activity#onActivityResult(int, int, Intent)}
     * @param data       The data intent from the {@link Activity#onActivityResult(int, int, Intent)}
     */
    @SuppressWarnings("JavadocReference")
    public void handleActivityResult(int resultCode, @Nullable Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                if (data == null) {
                    notifyException(new ComponentException("Result data is null"));
                    return;
                }

                final PaymentData paymentData = PaymentData.getFromIntent(data);
                final GooglePayInputData inputData = new GooglePayInputData();
                inputData.setPaymentData(paymentData);
                inputDataChanged(inputData);
                break;
            case Activity.RESULT_CANCELED:
                notifyException(new ComponentException("Payment canceled."));
                break;
            case AutoResolveHelper.RESULT_ERROR:
                final Status status = AutoResolveHelper.getStatusFromIntent(data);
                String errorMessage = "GooglePay returned an error";
                if (status != null) {
                    errorMessage = errorMessage.concat(": " + status.getStatusMessage());
                }
                notifyException(new ComponentException(errorMessage));
                break;
            default:
                // Do nothing.
        }
    }
}
