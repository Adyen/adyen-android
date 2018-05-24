package com.adyen.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.adyen.core.PaymentRequest;
import com.adyen.core.interfaces.PaymentDetailsCallback;
import com.adyen.core.interfaces.PaymentMethodCallback;
import com.adyen.core.interfaces.PaymentRequestDetailsListener;
import com.adyen.core.interfaces.UriCallback;
import com.adyen.core.internals.ModuleAvailabilityUtil;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.IdealPaymentDetails;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.core.models.paymentdetails.InputDetailsUtil;
import com.adyen.core.models.paymentdetails.IssuerSelectionPaymentDetails;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.adyen.core.services.PaymentMethodService;
import com.adyen.ui.activities.CheckoutActivity;
import com.adyen.ui.activities.RedirectHandlerActivity;
import com.adyen.ui.activities.TranslucentDialogActivity;
import com.adyen.ui.fragments.CreditCardFragmentBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.adyen.core.constants.Constants.DataKeys.CVC_FIELD_STATUS;
import static com.adyen.core.constants.Constants.DataKeys.GENERATION_TIME;
import static com.adyen.core.constants.Constants.DataKeys.PAYMENT_CARD_SCAN_ENABLED;
import static com.adyen.core.constants.Constants.DataKeys.PUBLIC_KEY;
import static com.adyen.core.constants.Constants.DataKeys.SHOPPER_REFERENCE;
import static com.adyen.core.constants.Constants.PaymentRequest.REDIRECT_HANDLED_INTENT;
import static com.adyen.core.constants.Constants.PaymentRequest.REDIRECT_PROBLEM_INTENT;
import static com.adyen.core.constants.Constants.PaymentRequest.REDIRECT_RETURN_URI_KEY;
import static com.adyen.ui.activities.CheckoutActivity.AMOUNT;
import static com.adyen.ui.activities.CheckoutActivity.FRAGMENT;
import static com.adyen.ui.activities.CheckoutActivity.PAYMENT_METHOD;
import static com.adyen.ui.activities.CheckoutActivity.PAYMENT_METHODS;
import static com.adyen.ui.activities.CheckoutActivity.PREFERED_PAYMENT_METHODS;


@SuppressWarnings({"unused", "WeakerAccess"})
public class DefaultPaymentRequestDetailsListener implements PaymentRequestDetailsListener {

    private static final String TAG = DefaultPaymentRequestDetailsListener.class.getSimpleName();

    private Context context;

    public DefaultPaymentRequestDetailsListener(Context context) {
            this.context = context;
    }

    @Override
    public void onPaymentMethodSelectionRequired(@NonNull PaymentRequest paymentRequest,
                                                 @NonNull List<PaymentMethod> preferredPaymentMethods,
                                                 @NonNull List<PaymentMethod> availablePaymentMethods,
                                                 @NonNull PaymentMethodCallback callback) {

        final Intent intent = new Intent(context.getApplicationContext(), CheckoutActivity.class);
        final Bundle bundle = new Bundle();
        ArrayList<PaymentMethod> preferredPaymentMethodsArrayList = new ArrayList<>();
        preferredPaymentMethodsArrayList.addAll(preferredPaymentMethods);
        ArrayList<PaymentMethod> availablePaymentMethodsArrayList = new ArrayList<>();
        availablePaymentMethodsArrayList.addAll(availablePaymentMethods);

        bundle.putSerializable(PREFERED_PAYMENT_METHODS, preferredPaymentMethodsArrayList);
        bundle.putSerializable(PAYMENT_METHODS, availablePaymentMethodsArrayList);
        bundle.putInt(FRAGMENT, CheckoutActivity.PAYMENT_METHOD_SELECTION_FRAGMENT);
        bundle.putSerializable(AMOUNT, paymentRequest.getAmount());

        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    @Override
    public void onRedirectRequired(@NonNull final PaymentRequest paymentRequest, @NonNull final String redirectUrl,
                                   @NonNull final UriCallback uriCallback) {

        Log.d(TAG, "Checkout SDK will handle redirection");
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REDIRECT_HANDLED_INTENT);
        intentFilter.addAction(REDIRECT_PROBLEM_INTENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (REDIRECT_HANDLED_INTENT.equals(intent.getAction())) {
                    final Uri returnUri = (Uri) intent.getExtras().get(REDIRECT_RETURN_URI_KEY);
                    Log.d(TAG, "RedirectHandled(redirection handled by Checkout SDK): " + returnUri);
                    uriCallback.completionWithUri(returnUri);
                    PackageManager pm = context.getPackageManager();
                    pm.setComponentEnabledSetting(new ComponentName(context, RedirectHandlerActivity.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                } else {
                    paymentRequest.cancel();
                }
            }
        }, intentFilter);

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, RedirectHandlerActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        final Intent redirectIntent = new Intent(context, RedirectHandlerActivity.class);
        redirectIntent.putExtra("url", redirectUrl);
        context.startActivity(redirectIntent);
    }

    @Override
    public void onPaymentDetailsRequired(@NonNull PaymentRequest paymentRequest,
                                         @NonNull Collection<InputDetail> inputDetails,
                                         @NonNull PaymentDetailsCallback callback) {

        if (InputDetailsUtil.containsKey(inputDetails, IdealPaymentDetails.IDEAL_ISSUER)
                || InputDetailsUtil.containsKey(inputDetails, IssuerSelectionPaymentDetails.ISSUER)) {
            final Intent intent = new Intent(context, CheckoutActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(PAYMENT_METHOD, paymentRequest.getPaymentMethod());
            bundle.putInt("fragment", CheckoutActivity.ISSUER_SELECTION_FRAGMENT);
            intent.putExtras(bundle);
            context.startActivity(intent);
        } else if (paymentRequest.getPaymentMethod().getType().equals(PaymentMethod.Type.SEPA_DIRECT_DEBIT)) {
            final Intent intent = new Intent(context, CheckoutActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(PAYMENT_METHOD, paymentRequest.getPaymentMethod());
            bundle.putSerializable(AMOUNT, paymentRequest.getAmount());
            bundle.putInt("fragment", CheckoutActivity.SEPA_DIRECT_DEBIT_FRAGMENT);
            intent.putExtras(bundle);
            context.startActivity(intent);
        } else if (paymentRequest.getPaymentMethod().getType().equals(PaymentMethod.Type.CARD)) {
            final Intent intent = new Intent(context, CheckoutActivity.class);
            intent.putExtra(FRAGMENT, CheckoutActivity.CREDIT_CARD_FRAGMENT);
            intent.putExtra(CheckoutActivity.PAYMENT_METHOD, paymentRequest.getPaymentMethod());
            intent.putExtra(AMOUNT, paymentRequest.getAmount());
            intent.putExtra(SHOPPER_REFERENCE, paymentRequest.getShopperReference());
            intent.putExtra(PUBLIC_KEY, paymentRequest.getPublicKey());
            intent.putExtra(GENERATION_TIME, paymentRequest.getGenerationTime());
            intent.putExtra(CVC_FIELD_STATUS, CreditCardFragmentBuilder.CvcFieldStatus.REQUIRED.name());
            intent.putExtra(PAYMENT_CARD_SCAN_ENABLED, true);
            context.startActivity(intent);
        } else if (paymentRequest.getPaymentMethod().getType().equals(PaymentMethod.Type.BCMC)) {
            final Intent intent = new Intent(context, CheckoutActivity.class);
            intent.putExtra(FRAGMENT, CheckoutActivity.CREDIT_CARD_FRAGMENT);
            intent.putExtra(CheckoutActivity.PAYMENT_METHOD, paymentRequest.getPaymentMethod());
            intent.putExtra(AMOUNT, paymentRequest.getAmount());
            intent.putExtra(SHOPPER_REFERENCE, paymentRequest.getShopperReference());
            intent.putExtra(PUBLIC_KEY, paymentRequest.getPublicKey());
            intent.putExtra(GENERATION_TIME, paymentRequest.getGenerationTime());
            intent.putExtra(CVC_FIELD_STATUS, CreditCardFragmentBuilder.CvcFieldStatus.NOCVC.name());
            context.startActivity(intent);
        } else if (paymentRequest.getPaymentMethod().getType().equals(PaymentMethod.Type.QIWI_WALLET)) {
            final Intent intent = new Intent(context, CheckoutActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(PAYMENT_METHOD, paymentRequest.getPaymentMethod());
            bundle.putSerializable(AMOUNT, paymentRequest.getAmount());
            bundle.putInt("fragment", CheckoutActivity.QIWI_WALLET_FRAGMENT);
            intent.putExtras(bundle);
            context.startActivity(intent);
        } else if (paymentRequest.getPaymentMethod().getType().equals(PaymentMethod.Type.PAYPAL)) {
            //We can set "storeDetails" to true if we want to set up a recurring contract.
            callback.completionWithPaymentDetails(new PaymentDetails(inputDetails));
        } else if (InputDetailsUtil.containsKey(inputDetails, "cardDetails.cvc")) {
            final Intent intent = new Intent(context, TranslucentDialogActivity.class);
            intent.putExtra(AMOUNT, paymentRequest.getAmount());
            intent.putExtra(CheckoutActivity.PAYMENT_METHOD, paymentRequest.getPaymentMethod());
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(intent);
        } else if (paymentRequest.getPaymentMethod().getPaymentModule() != null) {
            PaymentMethodService paymentMethodService = null;
            try {
                paymentMethodService = ModuleAvailabilityUtil.getModulePaymentService(
                        paymentRequest.getPaymentMethod().getPaymentModule());
            } catch (@NonNull final ClassNotFoundException e) {
                Log.e(TAG, "requestPaymentMethodDetails(): Payment module not found.", e);
            } catch (@NonNull final IllegalAccessException e) {
                Log.e(TAG, "requestPaymentMethodDetails(): IllegalAccessException occurred", e);
            } catch (@NonNull final InstantiationException e) {
                Log.e(TAG, "requestPaymentMethodDetails(): InstantiationException occurred", e);
            } catch (@NonNull NullPointerException nullPointerException) {
                Log.e(TAG, "requestPaymentMethodDetails(): Null pointer exception: ", nullPointerException);
            }

            if (paymentMethodService == null) {
                Toast.makeText(context, "Payment method not supported.", Toast.LENGTH_LONG).show();
            } else {
                // Pass the request to the correct module. The rest will be handled by the module.
                paymentMethodService.process(context, paymentRequest, paymentRequest.getPaymentRequestListener(), null);
            }
        } else {
            //This will work if all fields are optional.
            callback.completionWithPaymentDetails(new PaymentDetails(inputDetails));
        }
    }
}
