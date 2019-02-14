/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 24/11/2017.
 */

package com.adyen.checkout.ui.internal.giropay;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.NetworkingState;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.SearchHandler;
import com.adyen.checkout.core.model.GiroPayDetails;
import com.adyen.checkout.core.model.GiroPayIssuer;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.util.Adapter;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;
import com.adyen.checkout.ui.internal.common.util.image.Rembrandt;
import com.adyen.checkout.ui.internal.common.view.holder.TwoLineItemViewHolder;
import com.adyen.checkout.util.internal.SimpleTextWatcher;

import java.util.List;
import java.util.concurrent.Callable;

public class GiroPayDetailsActivity extends CheckoutDetailsActivity {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private static final String STATE_SELECTED_GIRO_PAY_ISSUER = "STATE_SELECTED_GIRO_PAY_ISSUER";

    private FrameLayout mSearchContainer;

    private AutoCompleteTextView mSearchStringAutoCompleteTextView;

    private ContentLoadingProgressBar mProgressBar;

    private TwoLineItemViewHolder mSelectedGiroPayIssuerViewHolder;

    private Adapter<GiroPayIssuer> mAutoCompleteAdapter;

    private TextView mErrorTextView;

    private Button mPayButton;

    private TextView mSurchargeTextView;

    private PaymentMethod mPaymentMethod;

    private SearchHandler<List<GiroPayIssuer>> mSearchHandler;

    private GiroPayIssuer mSelectedGiroPayIssuer;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, GiroPayDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @NonNull
    private static String formatGiroPayIssuer(@NonNull GiroPayIssuer giroPayIssuer) {
        return giroPayIssuer.getBankName().replaceAll("\\(.*\\)", "");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);
        mSearchHandler = SearchHandler.Factory.createGiroPayIssuerSearchHandler(getApplication(), mPaymentMethod);

        if (savedInstanceState != null) {
            mSelectedGiroPayIssuer = savedInstanceState.getParcelable(STATE_SELECTED_GIRO_PAY_ISSUER);
        }

        setContentView(R.layout.activity_giropay_details);
        setTitle(mPaymentMethod.getName());
        mSearchContainer = findViewById(R.id.frameLayout_searchContainer);
        mAutoCompleteAdapter = Adapter.forAutoCompleteTextView(new Adapter.TextDelegate<GiroPayIssuer>() {
            @NonNull
            @Override
            public String getText(@NonNull GiroPayIssuer giroPayIssuer) {
                return formatGiroPayIssuer(giroPayIssuer);
            }
        });
        mSearchStringAutoCompleteTextView = findViewById(R.id.autoCompleteTextView_searchString);
        mSearchStringAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchStringAutoCompleteTextView.showDropDown();
            }
        });
        mSearchStringAutoCompleteTextView.setAdapter(mAutoCompleteAdapter);
        mSearchStringAutoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (mAutoCompleteAdapter.getCount() == 1) {
                    mSelectedGiroPayIssuer = mAutoCompleteAdapter.getItem(0);
                    onGiroPayIssuerChanged();

                    return true;
                }

                return false;
            }
        });
        mSearchStringAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedGiroPayIssuer = mAutoCompleteAdapter.getItem(position);
                onGiroPayIssuerChanged();
            }
        });
        mSearchStringAutoCompleteTextView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchHandler.setSearchString(mSearchStringAutoCompleteTextView.getText().toString());
            }
        });
        mProgressBar = findViewById(R.id.contentLoadingProgressBar);
        ThemeUtil.applyPrimaryThemeColor(this, mProgressBar.getProgressDrawable(), mProgressBar.getIndeterminateDrawable());
        mSelectedGiroPayIssuerViewHolder = TwoLineItemViewHolder.create(findViewById(android.R.id.content), R.id.item_two_line);
        Callable<Drawable> logoCallable = getLogoApi().newBuilder(mPaymentMethod).buildCallable();
        Rembrandt.createDefaultLogoRequestArgs(getApplication(), logoCallable).into(this, mSelectedGiroPayIssuerViewHolder.getLogoImageView());
        mSelectedGiroPayIssuerViewHolder.getActionImageView().setImageResource(R.drawable.ic_primary_24dp);
        mSelectedGiroPayIssuerViewHolder.getActionImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedGiroPayIssuer = null;
                onGiroPayIssuerChanged();
            }
        });
        mErrorTextView = findViewById(R.id.textView_error);
        mPayButton = findViewById(R.id.button_continue);
        mPayButton.setEnabled(mSelectedGiroPayIssuer != null);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedGiroPayIssuer != null) {
                    GiroPayDetails giroPayDetails = new GiroPayDetails.Builder().setBic(mSelectedGiroPayIssuer.getBic()).build();
                    getPaymentHandler().initiatePayment(mPaymentMethod, giroPayDetails);
                }
            }
        });

        mSurchargeTextView = findViewById(R.id.textView_surcharge);

        PayButtonUtil.setPayButtonText(this, mPaymentMethod, mPayButton, mSurchargeTextView);

        mSearchHandler.getNetworkInfoObservable().observe(this, new Observer<NetworkingState>() {
            @Override
            public void onChanged(@NonNull NetworkingState networkingState) {
                if (networkingState.isExecutingRequests()) {
                    mProgressBar.show();
                } else {
                    mProgressBar.hide();
                }
            }
        });
        mSearchHandler.getSearchResultsObservable().observe(this, new Observer<List<GiroPayIssuer>>() {
            @Override
            public void onChanged(@NonNull List<GiroPayIssuer> giroPayIssuers) {
                mAutoCompleteAdapter.setItems(giroPayIssuers);

                if (giroPayIssuers.isEmpty()) {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    mErrorTextView.setText(getString(R.string.checkout_giropay_error_search_no_results));
                } else {
                    mErrorTextView.setVisibility(View.GONE);
                    mErrorTextView.setText(null);
                }
            }
        });
        mSearchHandler.getErrorObservable().observe(this, new Observer<CheckoutException>() {
            @Override
            public void onChanged(@NonNull CheckoutException e) {
                mErrorTextView.setVisibility(View.VISIBLE);
                mErrorTextView.setText(getString(R.string.checkout_error_message_default));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_SELECTED_GIRO_PAY_ISSUER, mSelectedGiroPayIssuer);
    }

    private void onGiroPayIssuerChanged() {
        if (mSelectedGiroPayIssuer == null) {
            mSearchContainer.setVisibility(View.VISIBLE);
            KeyboardUtil.showAndSelect(mSearchStringAutoCompleteTextView);
            mSelectedGiroPayIssuerViewHolder.getPrimaryTextView().setText(null);
            mSelectedGiroPayIssuerViewHolder.getSecondaryTextView().setText(null);
            mSelectedGiroPayIssuerViewHolder.itemView.setVisibility(View.GONE);
            updateContinueButton();

            if (mSearchStringAutoCompleteTextView.getWindowToken() != null) {
                mSearchStringAutoCompleteTextView.showDropDown();
            }
        } else {
            mSearchContainer.setVisibility(View.GONE);
            KeyboardUtil.hide(mSearchStringAutoCompleteTextView);
            mSelectedGiroPayIssuerViewHolder.getPrimaryTextView().setText(formatGiroPayIssuer(mSelectedGiroPayIssuer));
            mSelectedGiroPayIssuerViewHolder.getSecondaryTextView().setText(mSelectedGiroPayIssuer.getBic());
            mSelectedGiroPayIssuerViewHolder.itemView.setVisibility(View.VISIBLE);
            updateContinueButton();

            if (mSearchStringAutoCompleteTextView.getWindowToken() != null) {
                mSearchStringAutoCompleteTextView.dismissDropDown();
            }
        }
    }

    private void updateContinueButton() {
        boolean enabled = mSelectedGiroPayIssuer != null;
        mPayButton.setEnabled(enabled);
    }
}
