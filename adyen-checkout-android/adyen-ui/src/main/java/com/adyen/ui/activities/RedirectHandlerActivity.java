package com.adyen.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.adyen.core.constants.Constants;
import com.adyen.ui.R;
import com.adyen.utils.RedirectUtil;

import java.util.List;

import static com.adyen.core.constants.Constants.PaymentRequest.ADYEN_UI_FINALIZE_INTENT;

/**
 * Helper activity for handling redirection for the payment methods that require redirection.
 *
 * This class opens the URL on chrome custom tabs.
 */

public class RedirectHandlerActivity extends FragmentActivity {

    private static final String TAG = RedirectHandlerActivity.class.getSimpleName();

    private static final String CHROME_PACKAGE_NAME = "com.android.chrome";

    private Context context;

    public static final int CHROME_CUSTOM_TABS_REQUEST_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        final Intent intent = getIntent();
        final String urlString = intent.getStringExtra("url");
        if (!TextUtils.isEmpty(urlString)) {
            Intent redirectIntent = createRedirectIntent(urlString);
            redirectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(redirectIntent, CHROME_CUSTOM_TABS_REQUEST_CODE);

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

    private boolean useCustomTabsIntent() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(CHROME_PACKAGE_NAME, 0);
            String[] parts = packageInfo.versionName != null ? packageInfo.versionName.split("\\.") : new String[0];
            int version = Integer.parseInt(parts[0]);
            return version < 40 || version > 58;
        } catch (PackageManager.NameNotFoundException e) {
            // Not installed.
            return false;
        } catch (NumberFormatException e) {
            // Cannot parse version.
            return false;
        }
    }

    @NonNull
    private Intent createRedirectIntent(@NonNull String urlString) {
        Intent result;

        Uri uri = Uri.parse(urlString);
        RedirectUtil.ResolveResult resolveResult = RedirectUtil.determineResolveResult(this, uri);

        if (resolveResult.getResolveType() == RedirectUtil.ResolveType.APPLICATION) {
            // If the user has selected a default app for this URL, open with app.
            result = new Intent(Intent.ACTION_VIEW, uri);
        } else {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(this, R.color.white))
                    .build();
            customTabsIntent.intent.setData(uri);
            result = customTabsIntent.intent;

            List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(result, 0);

            if (useCustomTabsIntent()) {
                // Do not prompt user to select from all installed browsers, use ChromeCustomTabs with Chrome by default.
                for (ResolveInfo resolveInfo : resolveInfoList) {
                    if (CHROME_PACKAGE_NAME.equals(resolveInfo.activityInfo.packageName)) {
                        result.setPackage(CHROME_PACKAGE_NAME);
                        result.setClassName(CHROME_PACKAGE_NAME, resolveInfo.activityInfo.name);
                        break;
                    }
                }
            } else if (resolveInfoList.size() > 1) {
                // Create chooser with Chrome, but don't use CustomTabsIntent.
                Intent target = new Intent(Intent.ACTION_VIEW, uri);
                result = Intent.createChooser(target, null);
            }
        }

        return result;
    }

    private BroadcastReceiver uiFinalizationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this);
            finish();
        }
    };

}
