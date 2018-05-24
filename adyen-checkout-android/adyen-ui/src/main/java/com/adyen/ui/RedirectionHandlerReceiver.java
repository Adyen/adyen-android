package com.adyen.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.adyen.ui.activities.RedirectHandlerActivity;

import static com.adyen.core.constants.Constants.PaymentRequest.REDIRECT_PROBLEM_INTENT;
import static com.adyen.core.constants.Constants.PaymentRequest.REDIRECT_RETURN_URI_KEY;

/**
 * BroadcastReceiver for listening to "adyen.core.utils.DISABLE_REDIRECTION_HANDLER" events.
 *
 * When another instance of SDK (e.g. another application), wants to handle redirection, the other
 * instances should disable their redirection handlers if they are still active and cancel that
 * payment request.
 */
public class RedirectionHandlerReceiver extends BroadcastReceiver {

    private static final String TAG = RedirectionHandlerReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent == null) {
            return;
        }
        final String packageName = intent.getStringExtra("PackageName");
        PackageManager pm = context.getApplicationContext().getPackageManager();
        if (PackageManager.COMPONENT_ENABLED_STATE_ENABLED == pm.getComponentEnabledSetting(new ComponentName(context,
                RedirectHandlerActivity.class))
                && !context.getPackageName().equals(packageName)) {
            Log.d(TAG, "Disabling RedirectHandlerActivity for this application: " + context.getPackageName());
            pm.setComponentEnabledSetting(new ComponentName(context, RedirectHandlerActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            final Intent returnIntent = new Intent();
            returnIntent.setAction(REDIRECT_PROBLEM_INTENT);
            returnIntent.putExtra(REDIRECT_RETURN_URI_KEY, intent.getData());
            LocalBroadcastManager.getInstance(context).sendBroadcast(returnIntent);
        }

    }
}
