/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 16/10/2017.
 */

package com.adyen.checkout.ui.internal.common.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.RedirectDetails;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.RedirectUtil;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;

public class RedirectHandlerActivity extends AppCompatActivity {

    private static final String EXTRA_PAYMENT_REFERENCE = "EXTRA_PAYMENT_REFERENCE";

    private static final String EXTRA_REDIRECT_DETAILS = "EXTRA_REDIRECT_DETAILS";

    private static final int REQUEST_CODE_REDIRECT = 1;

    private static final String STATE_ON_ACTIVITY_RESULT_CALLED_FOR_REDIRECT = "STATE_ON_ACTIVITY_RESULT_CALLED_FOR_REDIRECT";

    private static final String STATE_ON_NEW_INTENT_CALLED_FOR_REDIRECT = "STATE_ON_NEW_INTENT_CALLED_FOR_REDIRECT";

    private static final int CHECK_REDIRECT_DELAY_MILLIS = 1500;

    private PaymentReference mPaymentReference;

    private PaymentHandler mPaymentHandler;

    private boolean mOnActivityResultCalledForRedirect;

    private boolean mOnNewIntentCalledForRedirect;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull RedirectDetails redirectDetails) {
        Intent intent = new Intent(context, RedirectHandlerActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_REDIRECT_DETAILS, redirectDetails);

        return intent;
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        // Don't finish here, instead wait for onNewIntent() or to be finished in onResume().
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(createContentView());
        overridePendingTransition(0, 0);

        mPaymentReference = getIntent().getParcelableExtra(EXTRA_PAYMENT_REFERENCE);

        if (mPaymentReference == null) {
            // If the app was killed by the shopper, fail gracefully.
            finish();
            return;
        }

        mPaymentHandler = mPaymentReference.getPaymentHandler(this);

        mOnActivityResultCalledForRedirect = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_ON_ACTIVITY_RESULT_CALLED_FOR_REDIRECT);
        mOnNewIntentCalledForRedirect = savedInstanceState != null && savedInstanceState.getBoolean(STATE_ON_NEW_INTENT_CALLED_FOR_REDIRECT);

        if (savedInstanceState == null) {
            RedirectDetails redirectDetails = getIntent().getParcelableExtra(EXTRA_REDIRECT_DETAILS);
            Intent redirectIntent = createRedirectIntent(redirectDetails.getUri());

            if (redirectIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(redirectIntent, REQUEST_CODE_REDIRECT);
            } else {
                Toast.makeText(getApplicationContext(), R.string.checkout_error_redirect_failed, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_REDIRECT) {
            mOnActivityResultCalledForRedirect = true;
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        Uri data = intent.getData();

        if (data != null) {
            mOnNewIntentCalledForRedirect = true;
            mPaymentHandler.handleRedirectResult(data);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mOnActivityResultCalledForRedirect && !mOnNewIntentCalledForRedirect) {
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        }, CHECK_REDIRECT_DELAY_MILLIS);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ON_ACTIVITY_RESULT_CALLED_FOR_REDIRECT, mOnActivityResultCalledForRedirect);
        outState.putBoolean(STATE_ON_NEW_INTENT_CALLED_FOR_REDIRECT, mOnNewIntentCalledForRedirect);
    }

    @NonNull
    private View createContentView() {
        int size = getResources().getDimensionPixelSize(R.dimen.progress_dialog_size);
        FrameLayout.LayoutParams progressBarLayoutParams = new FrameLayout.LayoutParams(size, size);
        progressBarLayoutParams.gravity = Gravity.CENTER;

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(progressBarLayoutParams);
        progressBar.setIndeterminate(true);

        ThemeUtil.applyPrimaryThemeColor(this, progressBar.getProgressDrawable(), progressBar.getIndeterminateDrawable());

        return progressBar;
    }

    @NonNull
    private Intent createRedirectIntent(@NonNull Uri uri) {
        if (RedirectUtil.determineResolveResult(this, uri).getResolveType() == RedirectUtil.ResolveType.APPLICATION) {
            return new Intent(Intent.ACTION_VIEW, uri);
        } else {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ThemeUtil.getPrimaryThemeColor(this))
                    .build();
            customTabsIntent.intent.setData(uri);

            return customTabsIntent.intent;
        }
    }
}
