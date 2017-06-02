package com.adyen.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.adyen.core.constants.Constants;
import com.adyen.ui.R;

import static com.adyen.core.constants.Constants.PaymentRequest.ADYEN_UI_FINALIZE_INTENT;

/**
 * Helper activity for handling redirection for the payment methods that require redirection.
 *
 * This class opens the URL on chrome custom tabs.
 */

public class RedirectHandlerActivity extends FragmentActivity {

    private static final String TAG = RedirectHandlerActivity.class.getSimpleName();
    private Context context;

    public static final int CHROME_CUSTOM_TABS_REQUEST_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        final Intent intent = getIntent();
        final String urlString = intent.getStringExtra("url");
        if (!TextUtils.isEmpty(urlString)) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(true).build();
            builder.setToolbarColor(ContextCompat.getColor(this, R.color.white));

            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            customTabsIntent.intent.setData(Uri.parse(urlString));
            //customTabsIntent.launchUrl(this, Uri.parse(urlString));
            startActivityForResult(customTabsIntent.intent, CHROME_CUSTOM_TABS_REQUEST_CODE);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(uiFinalizationReceiver,
                    new IntentFilter(ADYEN_UI_FINALIZE_INTENT));
        } else if (intent.getData() != null && intent.getData() instanceof Uri) {
            notifyReturnUriAndFinish(intent.getData());
        } else {
            Log.w(TAG, TAG + " has been started without any url. Exiting.");
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getData() != null && intent.getData() instanceof Uri) {
            notifyReturnUriAndFinish(intent.getData());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHROME_CUSTOM_TABS_REQUEST_CODE) {
            finish();
            final Intent intent = new Intent(this, CheckoutActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString("REDIRECT_RETURN", "REDIRECT_RETURN");

            intent.putExtras(bundle);

            startActivity(intent);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        finish();
    }

    private void notifyReturnUriAndFinish(final Uri uri) {
        final Intent returnIntent = new Intent();
        returnIntent.setAction(Constants.PaymentRequest.REDIRECT_HANDLED_INTENT);
        returnIntent.putExtra(Constants.PaymentRequest.REDIRECT_RETURN_URI_KEY, uri);
        returnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        LocalBroadcastManager.getInstance(context).sendBroadcast(returnIntent);
        finish();
    }

    private BroadcastReceiver uiFinalizationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this);
            finish();
        }
    };

}
