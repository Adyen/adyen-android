package com.adyen.checkout.ui.internal.giropay;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.internal.model.GiroPayIssuersResponse;
import com.adyen.checkout.core.model.GiroPayDetails;
import com.adyen.checkout.core.model.GiroPayIssuer;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.model.Operation;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.ui.internal.common.util.SearchUtil;
import com.adyen.checkout.ui.internal.common.util.SimpleArrayAdapter;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;
import com.adyen.checkout.ui.internal.common.util.image.Rembrandt;
import com.adyen.checkout.ui.internal.common.view.CustomTextInputLayout;
import com.adyen.checkout.ui.internal.common.view.holder.TwoLineItemViewHolder;
import com.adyen.checkout.util.internal.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 24/11/2017.
 */
public class GiroPayDetailsActivity extends CheckoutDetailsActivity {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private static final String STATE_GIRO_PAY_ISSUER = "STATE_GIRO_PAY_ISSUER";

    private static final int BIC_LENGTH = 11;

    private FrameLayout mSearchContainer;

    private CustomTextInputLayout mSearchStringCustomTextInputLayout;

    private AutoCompleteTextView mSearchStringAutoCompleteTextView;

    private ContentLoadingProgressBar mProgressBar;

    private TwoLineItemViewHolder mSelectedGiroPayIssuerViewHolder;

    private AutoCompleteAdapter mAutoCompleteAdapter;

    private TextView mErrorTextView;

    private Button mPayButton;

    private PaymentMethod mPaymentMethod;

    private GiroPayDetailsViewModel mViewModel;

    private GiroPayIssuer mGiroPayIssuer;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);
        GiroPayDetailsViewModel.Factory factory = new GiroPayDetailsViewModel.Factory(getApplication(), mPaymentMethod);
        mViewModel = new ViewModelProvider(getViewModelStore(), factory).get(GiroPayDetailsViewModel.class);

        if (savedInstanceState != null) {
            mGiroPayIssuer = savedInstanceState.getParcelable(STATE_GIRO_PAY_ISSUER);
        }

        setContentView(R.layout.activity_giropay_details);
        setTitle(mPaymentMethod.getName());
        mSearchContainer = findViewById(R.id.frameLayout_searchContainer);
        mSearchStringCustomTextInputLayout = findViewById(R.id.customTextInputLayout_searchString);
        mAutoCompleteAdapter = new AutoCompleteAdapter(this);
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
                    mGiroPayIssuer = mAutoCompleteAdapter.getItem(0);
                    onGiroPayIssuerChanged();

                    return true;
                }

                return false;
            }
        });
        mSearchStringAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mGiroPayIssuer = mAutoCompleteAdapter.getItem(position);
                onGiroPayIssuerChanged();
            }
        });
        mSearchStringAutoCompleteTextView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.getSearchStringLiveData().setValue(mSearchStringAutoCompleteTextView.getText().toString().trim());
            }
        });
        mProgressBar = findViewById(R.id.contentLoadingProgressBar);
        ThemeUtil.applyPrimaryThemeColor(this, mProgressBar.getProgressDrawable(), mProgressBar.getIndeterminateDrawable());
        mSelectedGiroPayIssuerViewHolder = TwoLineItemViewHolder.create(findViewById(android.R.id.content), R.id.item_two_line);
        Callable<Drawable> logoCallable = getLogoApi().newBuilder(mPaymentMethod).buildCallable();
        Rembrandt.createDefaultLogoRequestArgs(getApplication(), logoCallable).into(this, mSelectedGiroPayIssuerViewHolder.getLogoImageView());
        mSelectedGiroPayIssuerViewHolder.getActionImageView().setImageResource(R.drawable.ic_clear_black_24dp);
        mSelectedGiroPayIssuerViewHolder.getActionImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGiroPayIssuer = null;
                onGiroPayIssuerChanged();
            }
        });
        mErrorTextView = findViewById(R.id.textView_error);
        mPayButton = findViewById(R.id.button_continue);
        PayButtonUtil.setPayButtonText(this, mPayButton);
        mPayButton.setEnabled(mGiroPayIssuer != null);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GiroPayDetails giroPayDetails = null;

                if (mGiroPayIssuer != null) {
                    giroPayDetails = new GiroPayDetails.Builder().setBic(mGiroPayIssuer.getBic()).build();
                } else if (loadingFailedAndValidBicEntered()) {
                    giroPayDetails = new GiroPayDetails.Builder().setBic(mSearchStringAutoCompleteTextView.getText().toString()).build();
                }

                if (giroPayDetails != null) {
                    getPaymentHandler().initiatePayment(mPaymentMethod, giroPayDetails);
                }
            }
        });

        mViewModel.getQueryIssuersOperationLiveData().observe(this, new Observer<Operation<String, GiroPayIssuersResponse>>() {
            @Override
            public void onChanged(@Nullable Operation<String, GiroPayIssuersResponse> operation) {
                updateSearchStringCaption(operation);
                updateContentLoadingProgressBar(operation);
                updateErrorViews(operation);
                updateAdapter(operation);
                updateContinueButton();
            }
        });

        updateSearchStringCaption(null);
        updateSearchStringCaption(null);
        updateContentLoadingProgressBar(null);
        updateErrorViews(null);
        updateAdapter(null);
        updateContinueButton();
        onGiroPayIssuerChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_GIRO_PAY_ISSUER, mGiroPayIssuer);
    }

    private void onGiroPayIssuerChanged() {
        if (mGiroPayIssuer == null) {
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
            mSelectedGiroPayIssuerViewHolder.getPrimaryTextView().setText(formatGiroPayIssuer(mGiroPayIssuer));
            mSelectedGiroPayIssuerViewHolder.getSecondaryTextView().setText(mGiroPayIssuer.getBic());
            mSelectedGiroPayIssuerViewHolder.itemView.setVisibility(View.VISIBLE);
            updateContinueButton();

            if (mSearchStringAutoCompleteTextView.getWindowToken() != null) {
                mSearchStringAutoCompleteTextView.dismissDropDown();
            }
        }
    }

    private void updateSearchStringCaption(@Nullable Operation<String, GiroPayIssuersResponse> operation) {
        if (operation != null && operation.getError() != null) {
            mSearchStringCustomTextInputLayout.setCaption(R.string.checkout_giropay_search_hint_loading_failed);
        } else {
            mSearchStringCustomTextInputLayout.setCaption(R.string.checkout_giropay_search_hint);
        }
    }

    private void updateContentLoadingProgressBar(@Nullable Operation<String, GiroPayIssuersResponse> operation) {
        if (operation != null && operation.isRunning()) {
            mProgressBar.show();
        } else {
            mProgressBar.hide();
        }
    }

    private void updateErrorViews(@Nullable Operation<String, GiroPayIssuersResponse> operation) {
        if (operation == null) {
            mErrorTextView.setVisibility(View.GONE);
            mErrorTextView.setText(null);
        } else {
            operation.dispatchCurrentState(new Operation.Listener<String, GiroPayIssuersResponse>() {
                @Override
                public void onRunning(@NonNull String searchString) {
                    mErrorTextView.setVisibility(View.GONE);
                    mErrorTextView.setText(null);
                }

                @Override
                public void onComplete(@NonNull String searchString, @NonNull GiroPayIssuersResponse giroPayIssuersResponse) {
                    if (giroPayIssuersResponse.getGiroPayIssuers().isEmpty()) {
                        mErrorTextView.setVisibility(View.VISIBLE);
                        mErrorTextView.setText(getString(R.string.checkout_giropay_error_search_no_results));
                    } else {
                        mErrorTextView.setVisibility(View.GONE);
                        mErrorTextView.setText(null);
                    }
                }

                @Override
                public void onError(@NonNull String searchString, @Nullable GiroPayIssuersResponse giroPayIssuersResponse, @NonNull Throwable error) {
                    mErrorTextView.setVisibility(View.VISIBLE);
                    mErrorTextView.setText(error.getLocalizedMessage());
                }
            });
        }
    }

    private void updateAdapter(@Nullable Operation<String, GiroPayIssuersResponse> operation) {
        mAutoCompleteAdapter.clear();

        GiroPayIssuersResponse giroPayIssuersResponse = operation != null ? operation.getOutput() : null;

        if (giroPayIssuersResponse != null) {
            mAutoCompleteAdapter.addAll(giroPayIssuersResponse.getGiroPayIssuers());
        }
    }

    private void updateContinueButton() {
        boolean enabled = mGiroPayIssuer != null || loadingFailedAndValidBicEntered();
        mPayButton.setEnabled(enabled);
    }

    private boolean loadingFailedAndValidBicEntered() {
        Operation<String, GiroPayIssuersResponse> operation = mViewModel.getQueryIssuersOperationLiveData().getValue();
        return operation != null && operation.getError() != null
                && mSearchStringAutoCompleteTextView.getText().toString().replaceAll("\\s", "").length() == BIC_LENGTH;
    }

    private static final class AutoCompleteAdapter extends SimpleArrayAdapter<GiroPayIssuer> {
        // The lock from android.widget.ArrayAdapter is not accessible, use a new lock instead.
        private final Object mLock = new Object();

        private AutoCompleteAdapter(@NonNull Context context) {
            super(context);
        }

        @Override
        public void add(@Nullable GiroPayIssuer object) {
            synchronized (mLock) {
                super.add(object);
            }
        }

        @Override
        public void addAll(GiroPayIssuer... items) {
            synchronized (mLock) {
                super.addAll(items);
            }
        }

        @Override
        public void clear() {
            synchronized (mLock) {
                super.clear();
            }
        }

        @NonNull
        @Override
        protected String getText(@NonNull GiroPayIssuer item) {
            return formatGiroPayIssuer(item);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Adapter calls getView() instead of getDropDownView().
            return getDropDownView(position, convertView, parent);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    return formatGiroPayIssuer((GiroPayIssuer) resultValue);
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    List<GiroPayIssuer> bankInfos = new ArrayList<>();

                    synchronized (mLock) {
                        for (int i = 0; i < getCount(); i++) {
                            GiroPayIssuer bankInfo = getItem(i);

                            if (bankInfo != null && SearchUtil.anyMatches(constraint, bankInfo.getBankName(), bankInfo.getBic(), bankInfo.getBlz())) {
                                bankInfos.add(getItem(i));
                            }
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = bankInfos;
                    filterResults.count = bankInfos.size();

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    Object values = results.values;

                    if (values != null) {
                        // noinspection unchecked
                        addAll((List<GiroPayIssuer>) values);
                    }
                }
            };
        }
    }
}
