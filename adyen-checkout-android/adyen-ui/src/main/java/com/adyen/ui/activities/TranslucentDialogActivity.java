package com.adyen.ui.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.ui.views.CVCDialog;

import static com.adyen.core.constants.Constants.PaymentRequest.ADYEN_UI_FINALIZE_INTENT;

/**
 * Activity that contains a CVCDialog.
 * Should only be used to display a CVCDialog with transparent background.
 * Needs to have Amount and PaymentMethod in intent extras.
 */

public class TranslucentDialogActivity extends Activity {

    private CVCDialog cvcDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDialog(getIntent());
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(uiFinalizationReceiver,
                new IntentFilter(ADYEN_UI_FINALIZE_INTENT));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void setupDialog(final Intent intent) {
        if (cvcDialog != null && cvcDialog.isShowing()) {
            cvcDialog.dismiss();
        }
        Amount amount = (Amount) intent.getSerializableExtra(CheckoutActivity.AMOUNT);
        PaymentMethod paymentMethod = (PaymentMethod) intent.getSerializableExtra(CheckoutActivity.PAYMENT_METHOD);
        cvcDialog = new CVCDialog(this, amount, paymentMethod, new CVCDialog.CVCDialogListener() {
            @Override
            public void onDismissed() {
                finish();
            }
        });
        cvcDialog.show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cvcDialog != null && cvcDialog.isShowing()) {
            cvcDialog.dismiss();
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (cvcDialog != null && cvcDialog.isShowing()) {
            cvcDialog.dismiss();
        }
    }

    private BroadcastReceiver uiFinalizationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            finish();
        }
    };
}
