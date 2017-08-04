package com.adyen.ui.activities;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.adyen.core.constants.Constants;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.CreditCardPaymentDetails;
import com.adyen.core.models.paymentdetails.IdealPaymentDetails;
import com.adyen.core.models.paymentdetails.SepaDirectDebitPaymentDetails;
import com.adyen.ui.R;
import com.adyen.ui.fragments.CreditCardFragment;
import com.adyen.ui.fragments.CreditCardFragmentBuilder;
import com.adyen.ui.fragments.GiropayFragment;
import com.adyen.ui.fragments.IssuerSelectionFragment;
import com.adyen.ui.fragments.IssuerSelectionFragmentBuilder;
import com.adyen.ui.fragments.LoadingScreenFragment;
import com.adyen.ui.fragments.PaymentMethodSelectionFragment;
import com.adyen.ui.fragments.PaymentMethodSelectionFragmentBuilder;
import com.adyen.ui.fragments.SepaDirectDebitFragment;
import com.adyen.ui.fragments.SepaDirectDebitFragmentBuilder;

import java.util.ArrayList;

import static com.adyen.core.constants.Constants.PaymentRequest.ADYEN_UI_FINALIZE_INTENT;
import static com.adyen.core.constants.Constants.PaymentRequest.PAYMENT_DETAILS;
import static com.adyen.core.constants.Constants.PaymentRequest.PAYMENT_METHOD_SELECTED_INTENT;

/**
 * Activity for handling UI inside SDK.
 * This activity loads the appropriate fragment for the given state.
 */

public class CheckoutActivity extends FragmentActivity {

    private static final String TAG = CheckoutActivity.class.getSimpleName();

    // TODO: Get rid of this intent - dangerous.
    public static final int PAYMENT_METHOD_SELECTION_FRAGMENT = 0;
    public static final int CREDIT_CARD_FRAGMENT = 1;
    public static final int ISSUER_SELECTION_FRAGMENT = 2;
    public static final int SEPA_DIRECT_DEBIT_FRAGMENT = 3;
    public static final int GIROPAY_FRAGMENT = 4;
    public static final int LOADING_SCREEN_FRAGMENT = 11;

    public static final String PREFERED_PAYMENT_METHODS = "preferredPaymentMethods";
    public static final String PAYMENT_METHODS = "filteredPaymentMethods";
    public static final String AMOUNT = "amount";
    public static final String CARD_HOLDER_NAME_REQUIRED = "card_holder_name_required";
    public static final String ONE_CLICK = "oneClick";
    public static final String PAYMENT_METHOD = "PaymentMethod";
    public static final String FRAGMENT = "fragment";

    private boolean backButtonDisabled = false;

    private Context context;

    int currentFragment;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        context = this;
        backButtonDisabled = false;
        setupActionBar();
        final Intent intent = getIntent();

        initializeFragment(intent);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(uiFinalizationIntent,
                new IntentFilter(ADYEN_UI_FINALIZE_INTENT));
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        backButtonDisabled = false;
        Log.d(TAG, "onNewIntent()");
        if (intent.getExtras().containsKey("REDIRECT_RETURN")) {
            if (currentFragment == CREDIT_CARD_FRAGMENT) {
                final FragmentManager manager = getSupportFragmentManager();
                boolean fragmentPopped = manager.popBackStackImmediate(PaymentMethodSelectionFragment.class.getName(), 0);
            }
        } else {
            initializeFragment(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (backButtonDisabled) {
            Log.w(TAG, "Going back at this step is not possible.");
            return;
        }
        final int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 0) {
            super.onBackPressed();
        } else {
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().
                    getBackStackEntryAt(backStackEntryCount - 1);
            String tag = backEntry.getName();
            if (PaymentMethodSelectionFragment.class.getName().equals(tag)
                    || LoadingScreenFragment.class.getName().equals(tag)) {
                final Intent cancellationIntent = new Intent(Constants.PaymentRequest.PAYMENT_REQUEST_CANCELLED_INTENT);
                LocalBroadcastManager.getInstance(this).sendBroadcast(cancellationIntent);
                finish();
            } else if (backStackEntryCount == 1) {
                finish();
            } else {
                getSupportFragmentManager().popBackStackImmediate();
            }
        }
    }

    private BroadcastReceiver uiFinalizationIntent = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            finish();
        }
    };

    @SuppressWarnings("unchecked")
    private void initializeFragment(final Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            finish();
            return;
        }
        int fragmentId = bundle.getInt(FRAGMENT, -1);
        currentFragment = fragmentId;

        switch (fragmentId) {
            case PAYMENT_METHOD_SELECTION_FRAGMENT: {
                showActionBar();
                final ArrayList<PaymentMethod> preferredPaymentMethods = (ArrayList) bundle
                        .getSerializable(PREFERED_PAYMENT_METHODS);
                final ArrayList<PaymentMethod> paymentMethods = (ArrayList) bundle
                        .getSerializable(PAYMENT_METHODS);

                final PaymentMethodSelectionFragment paymentMethodSelectionFragment
                        = new PaymentMethodSelectionFragmentBuilder()
                        .setPaymentMethods(paymentMethods)
                        .setPreferredPaymentMethods(preferredPaymentMethods)
                        .setPaymentMethodSelectionListener(new PaymentMethodSelectionFragment.PaymentMethodSelectionListener() {
                            @Override
                            public void onPaymentMethodSelected(PaymentMethod paymentMethod) {
                                if (paymentMethod.isRedirectMethod()) {
                                    final Intent intent = new Intent(context.getApplicationContext(),
                                            TranslucentLoadingScreenActivity.class);
                                    context.startActivity(intent);
                                }
                                Intent intent = new Intent(PAYMENT_METHOD_SELECTED_INTENT);
                                intent.putExtra(PAYMENT_METHOD, paymentMethod);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        })
                        .build();



                replaceFragment(paymentMethodSelectionFragment);
                setActionBarTitle(R.string.title_payment_methods);

                hideKeyboard();
                break;
            }
            case CREDIT_CARD_FRAGMENT: {
                showActionBar();
                CreditCardFragment creditCardFragment = new CreditCardFragmentBuilder()
                        .setPaymentMethod((PaymentMethod) intent.getSerializableExtra(PAYMENT_METHOD))
                        .setPublicKey(intent.getStringExtra(Constants.DataKeys.PUBLIC_KEY))
                        .setGenerationtime(intent.getStringExtra(Constants.DataKeys.GENERATION_TIME))
                        .setAmount((Amount) intent.getSerializableExtra(AMOUNT))
                        .setShopperReference(intent.getStringExtra(Constants.DataKeys.SHOPPER_REFERENCE))
                        .setCreditCardInfoListener(new CreditCardFragment.CreditCardInfoListener() {
                            @Override
                            public void onCreditCardInfoProvided(CreditCardPaymentDetails creditCardPaymentDetails) {
                                final Intent intent = new Intent(Constants.PaymentRequest.PAYMENT_DETAILS_PROVIDED_INTENT);
                                intent.putExtra(PAYMENT_DETAILS, creditCardPaymentDetails);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                backButtonDisabled = true;
                            }
                        })
                        .build();


                replaceFragment(creditCardFragment);
                setActionBarTitle(R.string.title_card_details);

                break;
            }
            case ISSUER_SELECTION_FRAGMENT: {
                showActionBar();

                final PaymentMethod paymentMethod = (PaymentMethod) bundle.getSerializable(PAYMENT_METHOD);

                IssuerSelectionFragment issuerSelectionFragment = new IssuerSelectionFragmentBuilder()
                        .setPaymentMethod(paymentMethod)
                        .setIssuerSelectionListener(new IssuerSelectionFragment.IssuerSelectionListener() {
                            @Override
                            public void onIssuerSelected(String issuer) {
                                IdealPaymentDetails paymentDetails = new IdealPaymentDetails(paymentMethod.getInputDetails());
                                paymentDetails.fillIssuer(issuer);
                                final Intent intent = new Intent(Constants.PaymentRequest.PAYMENT_DETAILS_PROVIDED_INTENT);
                                intent.putExtra(PAYMENT_DETAILS, paymentDetails);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        })
                        .build();


                replaceFragment(issuerSelectionFragment);
                setActionBarTitle(R.string.title_issuers);
                break;
            }
            case SEPA_DIRECT_DEBIT_FRAGMENT: {
                showActionBar();

                final PaymentMethod paymentMethod = (PaymentMethod) bundle.getSerializable(PAYMENT_METHOD);

                SepaDirectDebitFragment sepaDirectDebitFragment = new SepaDirectDebitFragmentBuilder()
                        .setAmount((Amount) intent.getSerializableExtra(AMOUNT))
                        .setSEPADirectDebitPaymentDetailsListener(new SepaDirectDebitFragment.SEPADirectDebitPaymentDetailsListener() {
                            @Override
                            public void onPaymentDetails(String iban, String accountHolder) {
                                SepaDirectDebitPaymentDetails paymentDetails = new SepaDirectDebitPaymentDetails(paymentMethod.getInputDetails());
                                paymentDetails.fillIban(iban);
                                paymentDetails.fillOwner(accountHolder);

                                final Intent intent = new Intent(Constants.PaymentRequest.PAYMENT_DETAILS_PROVIDED_INTENT);
                                intent.putExtra(PAYMENT_DETAILS, paymentDetails);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        })
                        .build();

                replaceFragment(sepaDirectDebitFragment);
                setActionBarTitle(R.string.title_sepa);
                break;
            }
            case GIROPAY_FRAGMENT: {
                showActionBar();
                final GiropayFragment giropayFragment = new GiropayFragment();

                Bundle giroPayBundle = new Bundle();
                giroPayBundle.putSerializable(AMOUNT, intent.getSerializableExtra(AMOUNT));
                giropayFragment.setArguments(giroPayBundle);
                replaceFragment(giropayFragment);
                setActionBarTitle(R.string.title_giropay);
                break;
            }
            case LOADING_SCREEN_FRAGMENT: {
                hideActionBar();
                final LoadingScreenFragment loadingScreenFragment = new LoadingScreenFragment();
                replaceFragment(loadingScreenFragment);
                break;
            }
            default:
                throw new IllegalStateException("Unknown fragment selected");

        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.action_bar);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.getCustomView().findViewById(R.id.action_bar_back_icon).setOnClickListener(
                    new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void setActionBarTitle(int titleId) {
        if (getActionBar() != null && getActionBar().getCustomView() != null) {
            ((TextView) getActionBar().getCustomView().findViewById(R.id.action_bar_title))
                    .setText(getString(titleId));
        }
    }

    private void hideActionBar() {
        if (getActionBar() != null) {
            getActionBar().hide();
        }
    }

    private void showActionBar() {
        if (getActionBar() != null) {
            getActionBar().show();
        }
    }

    private void hideKeyboard() {
        if (getWindow() == null) {
            return;
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    private void replaceFragment(Fragment fragment) {
        final String backStateName = fragment.getClass().getName();
        final FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped) {
            //fragment not in back stack, create it.
            Log.d(TAG, "Starting fragment: " + backStateName);
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(android.R.id.content, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        } else {
            Log.d(TAG, "Fragment popped back");
        }
    }

}
