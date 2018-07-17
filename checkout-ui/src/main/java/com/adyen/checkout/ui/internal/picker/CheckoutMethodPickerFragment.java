package com.adyen.checkout.ui.internal.picker;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.fragment.CheckoutSessionFragment;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodPickerListener;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodsLiveData;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodsModel;
import com.adyen.checkout.ui.internal.common.model.CheckoutViewModel;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;
import com.adyen.checkout.ui.internal.common.util.recyclerview.DividerItemDecoration;
import com.adyen.checkout.ui.internal.common.util.recyclerview.HeaderItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/03/2018.
 */
public class CheckoutMethodPickerFragment extends CheckoutSessionFragment {
    public static final String TAG = "TAG_CHECKOUT_METHOD_PICKER_FRAGMENT";

    private CheckoutViewModel mCheckoutViewModel;

    private CheckoutMethodPickerAdapter mCheckoutMethodPickerAdapter;

    private RecyclerView mCheckoutMethodsRecyclerView;

    @NonNull
    public static CheckoutMethodPickerFragment newInstance(@NonNull PaymentReference paymentReference) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAYMENT_REFERENCE, paymentReference);

        CheckoutMethodPickerFragment fragment = new CheckoutMethodPickerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CheckoutMethodPickerListener) {
            CheckoutMethodPickerListener listener = (CheckoutMethodPickerListener) context;
            mCheckoutMethodPickerAdapter = new CheckoutMethodPickerAdapter(this, getLogoApi(), listener);
        } else {
            throw new RuntimeException(context.getClass() + " should implement " + CheckoutMethodPickerListener.class.getName());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        mCheckoutViewModel = ViewModelProviders.of(activity).get(CheckoutViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout_method_picker, container, false);

        Context context = inflater.getContext();

        final CheckoutMethodsLiveData checkoutMethodsLiveData = mCheckoutViewModel.getCheckoutMethodsLiveData();

        mCheckoutMethodsRecyclerView = view.findViewById(R.id.recyclerView_checkoutMethods);
        mCheckoutMethodsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mCheckoutMethodsRecyclerView.setAdapter(mCheckoutMethodPickerAdapter);
        mCheckoutMethodsRecyclerView.addItemDecoration(
                new DividerItemDecoration(context, new DividerItemDecoration.Callback() {
                    @Override
                    public boolean isDividerPosition(int position) {
                        return position == checkoutMethodsLiveData.getOneClickCheckoutMethodCount() - 1;
                    }
                })
        );
        TextView oneClickHeaderTextView = createHeaderTextView(R.string.checkout_one_click_payment_method_section_title);
        mCheckoutMethodsRecyclerView.addItemDecoration(
                new HeaderItemDecoration(oneClickHeaderTextView, new HeaderItemDecoration.Callback() {
                    @Override
                    public boolean isHeaderPosition(int position) {
                        return checkoutMethodsLiveData.getOneClickCheckoutMethodCount() > 0 && position == 0;
                    }
                })
        );
        TextView paymentMethodsWithOneClickHeaderTextView = createHeaderTextView(R.string.checkout_payment_method_section_with_one_click_title);
        mCheckoutMethodsRecyclerView.addItemDecoration(
                new HeaderItemDecoration(paymentMethodsWithOneClickHeaderTextView, new HeaderItemDecoration.Callback() {
                    @Override
                    public boolean isHeaderPosition(int position) {
                        int oneClickCheckoutMethodCount = checkoutMethodsLiveData.getOneClickCheckoutMethodCount();
                        return oneClickCheckoutMethodCount > 0 && position == oneClickCheckoutMethodCount;
                    }
                })
        );
        mCheckoutMethodsRecyclerView.addItemDecoration(
                new HeaderItemDecoration(createHeaderTextView(R.string.checkout_payment_method_section_title), new HeaderItemDecoration.Callback() {
                    @Override
                    public boolean isHeaderPosition(int position) {
                        int oneClickCheckoutMethodCount = checkoutMethodsLiveData.getOneClickCheckoutMethodCount();
                        return position == oneClickCheckoutMethodCount && oneClickCheckoutMethodCount == 0;
                    }
                })
        );

        checkoutMethodsLiveData.observe(this, new Observer<CheckoutMethodsModel>() {
                    @Override
                    public void onChanged(@Nullable final CheckoutMethodsModel checkoutMethodsModel) {
                        List<CheckoutMethod> allCheckoutMethods = (checkoutMethodsModel != null)
                                ? checkoutMethodsModel.getAllCheckoutMethods()
                                : new ArrayList<CheckoutMethod>();
                        mCheckoutMethodPickerAdapter.updateCheckoutMethods(allCheckoutMethods);
                    }
                }
        );

        return view;
    }

    public int getDesiredPeekHeight() {
        int maxPeekHeight = getResources().getDisplayMetrics().heightPixels * 2 / 3;
        int peekHeight = 0;
        int childIndex = 0;
        int childCount = mCheckoutMethodsRecyclerView.getAdapter().getItemCount();

        while (peekHeight < maxPeekHeight && childIndex < childCount) {
            View child = mCheckoutMethodsRecyclerView.getLayoutManager().findViewByPosition(childIndex++);

            if (child != null) {
                peekHeight = child.getBottom();

                if (peekHeight > maxPeekHeight) {
                    peekHeight -= child.getMeasuredHeight() / 2;
                }
            }
        }

        return peekHeight;
    }

    @NonNull
    private TextView createHeaderTextView(@StringRes int textResId) {
        Context context = mCheckoutMethodsRecyclerView.getContext();
        int padding = getResources().getDimensionPixelSize(R.dimen.standard_margin);
        TextView headerTextView = new AppCompatTextView(context);
        headerTextView.setPadding(padding, padding, padding, padding);
        headerTextView.setTextColor(ThemeUtil.getPrimaryThemeColor(context));

        if (textResId != 0) {
            headerTextView.setText(textResId);
        }

        return headerTextView;
    }
}
