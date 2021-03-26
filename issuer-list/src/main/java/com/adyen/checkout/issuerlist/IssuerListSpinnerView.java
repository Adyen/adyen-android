/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/4/2019.
 */

package com.adyen.checkout.issuerlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.adyen.checkout.components.GenericComponentState;
import com.adyen.checkout.components.api.ImageLoader;
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod;
import com.adyen.checkout.components.ui.view.AdyenLinearLayout;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.util.Collections;
import java.util.List;

public abstract class IssuerListSpinnerView<
        IssuerListPaymentMethodT extends IssuerListPaymentMethod,
        IssuerListComponentT extends IssuerListComponent<IssuerListPaymentMethodT>>
        extends AdyenLinearLayout<
            IssuerListOutputData,
            IssuerListConfiguration,
            GenericComponentState<IssuerListPaymentMethodT>,
            IssuerListComponentT>
        implements AdapterView.OnItemSelectedListener {
    private static final String TAG = LogUtil.getTag();

    private AppCompatSpinner mIssuersSpinner;

    private final IssuerListInputData mIdealInputData = new IssuerListInputData();

    private IssuerListSpinnerAdapter mIssuersAdapter;

    public IssuerListSpinnerView(@NonNull Context context) {
        this(context, null);
    }

    public IssuerListSpinnerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public IssuerListSpinnerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.issuer_list_spinner_view, this, true);
    }

    @Override
    public void initView() {
        mIssuersSpinner = findViewById(R.id.spinner_issuers);
        mIssuersSpinner.setAdapter(mIssuersAdapter);
        mIssuersSpinner.setOnItemSelectedListener(this);
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    protected void initLocalizedStrings(@NonNull Context localizedContext) {
        // no embedded localized strings on this view
    }

    @Override
    public void onComponentAttached() {
        mIssuersAdapter = new IssuerListSpinnerAdapter(getContext(),
                Collections.emptyList(),
                ImageLoader.getInstance(getContext(), getComponent().getConfiguration().getEnvironment()),
                getComponent().getPaymentMethodType(),
                hideIssuersLogo());
    }

    @Override
    protected void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner) {
        getComponent().getIssuersLiveData().observe(lifecycleOwner, createIssuersObserver());
    }

    @Override
    public boolean isConfirmationRequired() {
        return true;
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public void highlightValidationErrors() {
        // no implementation
    }

    public boolean hideIssuersLogo() {
        return false;
    }

    void onIssuersChanged(@NonNull List<IssuerModel> issuerList) {
        mIssuersAdapter.updateIssuers(issuerList);
    }

    @Override
    public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
        Logger.d(TAG, "onItemSelected - " + mIssuersAdapter.getItem(position).getName());
        mIdealInputData.setSelectedIssuer(mIssuersAdapter.getItem(position));
        getComponent().inputDataChanged(mIdealInputData);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mIssuersSpinner.setEnabled(enabled);
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public void onNothingSelected(@NonNull AdapterView<?> parent) {
        // nothing changed
    }

    private Observer<List<IssuerModel>> createIssuersObserver() {
        return this::onIssuersChanged;
    }

}
