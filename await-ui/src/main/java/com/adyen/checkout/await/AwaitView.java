/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */

package com.adyen.checkout.await;

import android.annotation.SuppressLint;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adyen.checkout.await.ui.R;
import com.adyen.checkout.base.ActionComponentData;
import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.ui.view.AdyenLinearLayout;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

public class AwaitView extends AdyenLinearLayout<AwaitOutputData, AwaitConfiguration, ActionComponentData, AwaitComponent>
        implements Observer<AwaitOutputData> {
    private static final String TAG = LogUtil.getTag();

    @SuppressLint(Lint.SYNTHETIC)
    ImageView mImageView;
    @SuppressLint(Lint.SYNTHETIC)
    TextView mTextViewOpenApp;
    @SuppressLint(Lint.SYNTHETIC)
    TextView mTextViewWaitingConfirmation;

    private ImageLoader mImageLoader;
    private String mPaymentMethodType;

    public AwaitView(@NonNull Context context) {
        this(context, null);
    }

    public AwaitView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public AwaitView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.await_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_double_margin);
        setPadding(padding, padding, padding, padding);
    }

    @Override
    public void onComponentAttached() {
        mImageLoader = ImageLoader.getInstance(getContext(), getComponent().getConfiguration().getEnvironment());
    }

    @Override
    public void initView() {
        mImageView = findViewById(R.id.imageView_logo);
        mTextViewOpenApp = findViewById(R.id.textView_open_app);
        mTextViewWaitingConfirmation = findViewById(R.id.textView_waiting_confirmation);
    }

    @Override
    protected void initLocalizedStrings(@NonNull Context localizedContext) {
        // TODO: 02/09/2020 IMPLEMENT
    }

    @Override
    protected void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner) {
        getComponent().observeOutputData(lifecycleOwner, this);
    }

    @Override
    public void onChanged(@Nullable AwaitOutputData awaitOutputData) {
        Logger.d(TAG, "onChanged");
        if (awaitOutputData == null) {
            return;
        }

        if (mPaymentMethodType == null || !mPaymentMethodType.equals(awaitOutputData.getPaymentMethodType())) {
            mPaymentMethodType = awaitOutputData.getPaymentMethodType();
            updateLogo();
        }
    }

    @Override
    public boolean isConfirmationRequired() {
        return false;
    }

    @Override
    public void highlightValidationErrors() {
        // No validation required
    }

    private void updateLogo() {
        Logger.d(TAG, "updateLogo - " + mPaymentMethodType);
        if (!TextUtils.isEmpty(mPaymentMethodType)) {
            mImageLoader.load(mPaymentMethodType, mImageView);
        }
    }
}
