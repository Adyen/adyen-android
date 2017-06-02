package com.adyen.androidpay.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.adyen.androidpay.R;
import com.adyen.core.models.Amount;
import com.adyen.core.utils.AmountUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AndroidPayActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = AndroidPayActivity.class.getSimpleName();

    private static final int REQUEST_CODE_MASKED_WALLET = 1001;
    private static final int REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET = 1004;

    private MaskedWallet maskedWallet;
    private FullWallet fullWallet;
    private SupportWalletFragment supportWalletFragment;
    private GoogleApiClient googleApiClient;

    private String publicKey;
    private String cartTotal;
    private String merchantName;
    private String currency;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_pay);

        Intent intent = getIntent();

        publicKey = intent.getExtras().getString("publicKey");

        Amount amount = (Amount) intent.getExtras().getSerializable("amount");
        //Locale.US is intentional. AndroidPay expects prices in format "2.00" regardless of locale.
        cartTotal = AmountUtil.format(amount, false, Locale.US);
        currency = amount.getCurrency();
        merchantName = intent.getExtras().getString("merchantName");

        googleApiClient = getGoogleApiClient();

        supportWalletFragment = createWalletFragment(WalletConstants.ENVIRONMENT_TEST,
                WalletFragmentStyle.BuyButtonAppearance.ANDROID_PAY_DARK, WalletConstants.THEME_DARK);
        // add Wallet fragment to the UI
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.android_pay_fragment_container, supportWalletFragment)
                .commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "Request code: " + requestCode + ", result code: " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_MASKED_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (data != null) {
                            maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                            if (maskedWallet != null) {
                                googleApiClient.connect();
                                FullWalletRequest fullWalletRequest
                                        = createFullWalletRequest(maskedWallet.getGoogleTransactionId());
                                // [START load_full_wallet]
                                Wallet.Payments.loadFullWallet(googleApiClient, fullWalletRequest,
                                        REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET);
                                // [END load_full_wallet]
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        //paymentRequestListener.onPaymentResult(paymentRequest,
                        // new PaymentResult(new Throwable(errorCode + ": Android Pay error")));
                        break;
                }
                break;
            case WalletConstants.RESULT_ERROR:
                //paymentRequestListener.onPaymentResult(paymentRequest,
                // new PaymentResult(new Throwable(errorCode + ": Android Pay error")));
                break;
            case REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
                final Intent intent = new Intent("com.adyen.androidpay.ui.AndroidTokenProvided");
                switch (resultCode) {
                    case RESULT_OK:
                        if (data != null) {
                            fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                            intent.putExtra("androidpay.token", fullWallet.getPaymentMethodToken().getToken());
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                        break;
                    case WalletConstants.RESULT_ERROR:
                        intent.putExtra("androidpay.error", "An error occurred with AndroidPay");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        break;
                    default:
                        intent.putExtra("androidpay.error", "Unknown result code");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        break;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * Creates the WalletFragment to be added to the UI in order to display the Android Pay button.
     *
     * @param walletEnvironment
     * @param buyButtonAppearance
     * @param walletTheme
     * @return {@link SupportWalletFragment}
     */
    @NonNull
    public SupportWalletFragment createWalletFragment(int walletEnvironment,
                                                      int buyButtonAppearance,
                                                      int walletTheme) {
        // [START fragment_style_and_options]
        WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setBuyButtonText(WalletFragmentStyle.BuyButtonText.BUY_WITH)
                .setBuyButtonAppearance(buyButtonAppearance)
                .setBuyButtonHeight(150)
                .setBuyButtonWidth(WalletFragmentStyle.Dimension.MATCH_PARENT);

        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(walletEnvironment)
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(walletTheme)
                .setMode(WalletFragmentMode.BUY_BUTTON)
                .build();
        final SupportWalletFragment walletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);
        // [END fragment_style_and_options]

        // Now initialize the Wallet Fragment
        MaskedWalletRequest maskedWalletRequest;

        // Direct integration
        maskedWalletRequest = createMaskedWalletRequest(publicKey);

        // [START params_builder]
        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                .setMaskedWalletRequest(maskedWalletRequest)
                .setMaskedWalletRequestCode(REQUEST_CODE_MASKED_WALLET)
                .setAccountName(null);
        walletFragment.initialize(startParamsBuilder.build());
        // [END params_builder]

        return walletFragment;
    }

    /**
     * Creates a MaskedWalletRequest for direct merchant integration (no payment processor).
     *
     * @param publicKey base64-encoded public encryption key. See instructions for more details.
     * @return {@link MaskedWalletRequest} instance
     */
    private MaskedWalletRequest createMaskedWalletRequest(@Nullable String publicKey) {
        // Validate the public key
        if (publicKey == null || publicKey.contains("REPLACE_ME")) {
            throw new IllegalArgumentException("Invalid public key, see README for instructions.");
        }

        // Create direct integration parameters
        // [START direct_integration_parameters]
        PaymentMethodTokenizationParameters parameters =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.NETWORK_TOKEN)
                        .addParameter("publicKey", publicKey)
                        .build();
        // [END direct_integration_parameters]

        return createMaskedWalletRequest(parameters);
    }

    private MaskedWalletRequest createMaskedWalletRequest(PaymentMethodTokenizationParameters parameters) {

        // [START masked_wallet_request]
        return MaskedWalletRequest.newBuilder()
                .setMerchantName(merchantName)
                .setPhoneNumberRequired(true)
                .setShippingAddressRequired(true)
                .setCurrencyCode(currency)
                .setEstimatedTotalPrice(cartTotal)
                .setPaymentMethodTokenizationParameters(parameters)
                .build();
        // [END masked_wallet_request]
    }

    /**
     * @param googleTransactionId
     * @return {@link FullWalletRequest} instance
     */
    public FullWalletRequest createFullWalletRequest(String googleTransactionId) {

        // [START full_wallet_request]
        return FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode(currency)
                        .setTotalPrice(cartTotal)
                        /*
                    .addLineItem(
                            LineItem.newBuilder()
                                    .setCurrencyCode("GBP")
                                    .setQuantity("2")
                                    .setUnitPrice("0.50")
                                    .setTotalPrice("1.00")
                                    .setDescription("Description")
                                .build())
                        */
                        .build())
                .build();
        // [END full_wallet_request]
    }

    /**
     * Return the Google API Client.
     *
     * @return {@link GoogleApiClient}
     */
    @NonNull
    public GoogleApiClient getGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder().build())
                .addConnectionCallbacks(this)
                .build();
    }

    /**
     * Returns filled in Payment Data for Android Pay.
     *
     * @return {@link Map <String, Object>}
     */
    @NonNull
    public Map<String, Object> getPaymentData() {
        final Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", Float.parseFloat(cartTotal));
        paymentData.put("currency", currency);
        return paymentData;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed:" + connectionResult.getErrorMessage());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected: Google API client is connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended: Google API client connection is suspended");
    }

}
