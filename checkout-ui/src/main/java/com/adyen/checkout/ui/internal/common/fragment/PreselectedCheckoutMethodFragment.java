package com.adyen.checkout.ui.internal.common.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodPickerListener;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodsModel;
import com.adyen.checkout.ui.internal.common.model.CheckoutViewModel;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.ui.internal.common.view.holder.TwoLineItemViewHolder;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 05/04/2018.
 */
public class PreselectedCheckoutMethodFragment extends CheckoutSessionFragment {
    public static final String TAG = "PRESELECTED_CHECKOUT_METHOD_FRAGMENT";

    private TwoLineItemViewHolder mTwoLineItemViewHolder;

    private Button mConfirmButton;

    private Button mSelectOtherPaymentMethodButton;

    private CheckoutViewModel mCheckoutViewModel;

    private CheckoutMethod mCheckoutMethod;

    @NonNull
    public static PreselectedCheckoutMethodFragment newInstance(@NonNull PaymentReference paymentReference) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAYMENT_REFERENCE, paymentReference);

        PreselectedCheckoutMethodFragment fragment = new PreselectedCheckoutMethodFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        mCheckoutViewModel = ViewModelProviders.of(activity).get(CheckoutViewModel.class);
        mCheckoutViewModel.getCheckoutMethodsLiveData().observe(this, new Observer<CheckoutMethodsModel>() {
            @Override
            public void onChanged(@Nullable CheckoutMethodsModel checkoutMethodsModel) {
                setCheckoutMethod(checkoutMethodsModel != null ? checkoutMethodsModel.getPreselectedCheckoutMethod() : null);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preselected_checkout_method, container, false);

        mTwoLineItemViewHolder = TwoLineItemViewHolder.create(view, R.id.item_two_line);

        mConfirmButton = view.findViewById(R.id.button_confirm);
        PayButtonUtil.setPayButtonText(this, mConfirmButton);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckoutMethod == null) {
                    return;
                }

                FragmentActivity activity = getActivity();

                if (activity instanceof CheckoutMethodPickerListener) {
                    ((CheckoutMethodPickerListener) activity).onCheckoutMethodSelected(mCheckoutMethod);
                }
            }
        });

        mSelectOtherPaymentMethodButton = view.findViewById(R.id.button_selectOtherPaymentMethod);
        mSelectOtherPaymentMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = getActivity();

                if (activity instanceof CheckoutMethodPickerListener) {
                    mCheckoutViewModel.getCheckoutMethodsLiveData().setPreselectedCheckoutMethodCleared();
                    ((CheckoutMethodPickerListener) activity).onClearSelection();
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        setCheckoutMethod(mCheckoutViewModel.getCheckoutMethodsLiveData().getPreselectedCheckoutMethod());
    }

    private void setCheckoutMethod(@Nullable CheckoutMethod checkoutMethod) {
        mCheckoutMethod = checkoutMethod;

        if (mCheckoutMethod != null) {
            mCheckoutMethod.buildLogoRequestArgs(getLogoApi()).into(this, mTwoLineItemViewHolder.getLogoImageView());
            mTwoLineItemViewHolder.setPrimaryText(mCheckoutMethod.getPrimaryText());
            mTwoLineItemViewHolder.setSecondaryText(mCheckoutMethod.getSecondaryText());
        }
    }
}
