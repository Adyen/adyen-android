/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/5/2019.
 */

package com.adyen.checkout.issuerlist;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.model.payments.request.IssuerListPaymentMethod;
import com.adyen.checkout.base.ui.adapter.ClickableListRecyclerAdapter;
import com.adyen.checkout.base.ui.view.AdyenLinearLayout;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.issuerlist.ui.R;

import java.util.Collections;
import java.util.List;

public abstract class IssuerListRecyclerView
        <IssuerListPaymentMethodT extends IssuerListPaymentMethod, IssuerListComponentT extends IssuerListComponent<IssuerListPaymentMethodT>>
        extends AdyenLinearLayout<IssuerListOutputData, IssuerListConfiguration, PaymentComponentState, IssuerListComponentT>
        implements Observer<List<IssuerModel>>, ClickableListRecyclerAdapter.OnItemCLickedListener {
    private static final String TAG = LogUtil.getTag();

    private RecyclerView mIssuersRecyclerView;
    private IssuerListRecyclerAdapter mIssuersAdapter;

    private final IssuerListInputData mIdealInputData = new IssuerListInputData();

    public IssuerListRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public IssuerListRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public IssuerListRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.issuer_list_recycler_view, this, true);
    }

    @Override
    public void initView() {
        mIssuersRecyclerView = findViewById(R.id.recycler_issuers);
        mIssuersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mIssuersAdapter.setItemCLickListener(this);
        mIssuersRecyclerView.setAdapter(mIssuersAdapter);
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    protected void initLocalizedStrings(@NonNull Context localizedContext) {
        // no embedded localized strings on this view
    }

    @Override
    public void onComponentAttached() {
        mIssuersAdapter = new IssuerListRecyclerAdapter(Collections.<IssuerModel>emptyList(),
                ImageLoader.getInstance(getContext(), getComponent().getConfiguration().getEnvironment()),
                getComponent().getPaymentMethod().getType(),
                hideIssuersLogo());
    }

    @Override
    protected void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner) {
        getComponent().getIssuersLiveData().observe(lifecycleOwner, this);

    }

    @Override
    public boolean isConfirmationRequired() {
        return false;
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public void highlightValidationErrors() {
        // no implementation
    }

    public boolean hideIssuersLogo() {
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mIssuersRecyclerView.setEnabled(enabled);
    }

    @Override
    public void onChanged(@Nullable List<IssuerModel> issuerModels) {
        Logger.v(TAG, "onChanged");
        if (issuerModels == null) {
            Logger.e(TAG, "issuerModels is null");
            return;
        }

        mIssuersAdapter.updateIssuerModelList(issuerModels);
    }

    @NonNull
    protected Observer<IssuerListOutputData> createOutputDataObserver() {
        return new Observer<IssuerListOutputData>() {
            @Override
            public void onChanged(@Nullable IssuerListOutputData idealOutputData) {
                // Component does not change selected IssuerModel, add validation UI here if that's needed
            }
        };
    }

    @Override
    public void onItemClicked(int position) {
        Logger.d(TAG, "onItemClicked - " + position);
        mIdealInputData.setSelectedIssuer(mIssuersAdapter.getIssuerAt(position));
        getComponent().inputDataChanged(mIdealInputData);
    }
}
