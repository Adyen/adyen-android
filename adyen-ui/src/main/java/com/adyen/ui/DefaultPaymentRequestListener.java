package com.adyen.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.adyen.core.PaymentRequest;
import com.adyen.core.interfaces.PaymentDataCallback;
import com.adyen.core.interfaces.PaymentRequestListener;
import com.adyen.core.models.PaymentRequestResult;
import com.adyen.ui.activities.CheckoutActivity;

import static com.adyen.core.constants.Constants.PaymentRequest.ADYEN_UI_FINALIZE_INTENT;


@SuppressWarnings({"unused", "WeakerAccess"})
public class DefaultPaymentRequestListener implements PaymentRequestListener {

    private Context context;

    public DefaultPaymentRequestListener(Context context) {
        this.context = context;
    }

    @Override
    public void onPaymentDataRequested(@NonNull PaymentRequest paymentRequest, @NonNull String sdkToken,
                                       @NonNull PaymentDataCallback callback) {
        Intent intent = new Intent(context.getApplicationContext(), CheckoutActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(CheckoutActivity.FRAGMENT, CheckoutActivity.LOADING_SCREEN_FRAGMENT);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onPaymentResult(@NonNull PaymentRequest paymentRequest, @NonNull PaymentRequestResult result) {
        Intent uiFinalizeIntent = new Intent(ADYEN_UI_FINALIZE_INTENT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(uiFinalizeIntent);
    }
}
