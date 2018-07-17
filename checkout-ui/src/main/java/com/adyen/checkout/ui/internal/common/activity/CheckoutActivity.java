package com.adyen.checkout.ui.internal.common.activity;

import android.animation.ValueAnimator;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.PaymentResult;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.fragment.CheckoutDetailsFragment;
import com.adyen.checkout.ui.internal.common.fragment.PreselectedCheckoutMethodFragment;
import com.adyen.checkout.ui.internal.common.model.CheckoutHandler;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethod;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodPickerListener;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodsModel;
import com.adyen.checkout.ui.internal.common.model.CheckoutViewModel;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;
import com.adyen.checkout.ui.internal.picker.CheckoutMethodPickerFragment;

import java.util.List;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 22/08/2017.
 */
public class CheckoutActivity extends CheckoutSessionActivity implements CheckoutMethodPickerListener, CheckoutHandler {
    private static final int REQUEST_CODE_PAYMENT_METHOD_DETAILS = 1;

    private CheckoutViewModel mCheckoutViewModel;

    private View mBottomSheetView;

    private ContentLoadingProgressBar mProgressBar;

    @VisibleForTesting
    public BottomSheetBehavior mBottomSheetBehavior;

    private ValueAnimator mBottomSheetValueAnimator;

    private int mTargetPeekHeight;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference) {
        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);

        return intent;
    }

    @Override
    public void onCheckoutMethodSelected(@NonNull CheckoutMethod checkoutMethod) {
        checkoutMethod.onSelected(this);
    }

    @Override
    public void onCheckoutMethodDelete(@NonNull CheckoutMethod checkoutMethod) {
        getPaymentHandler().deleteOneClickPaymentMethod(checkoutMethod.getPaymentMethod());
    }

    @Override
    public boolean isCheckoutMethodDeletable(@NonNull CheckoutMethod checkoutMethod) {
        PaymentSession paymentSession = getPaymentSession();

        if (paymentSession == null) {
            return false;
        }

        List<PaymentMethod> oneClickPaymentMethods = paymentSession.getOneClickPaymentMethods();

        return oneClickPaymentMethods != null && oneClickPaymentMethods.contains(checkoutMethod.getPaymentMethod());
    }

    @Override
    public void onClearSelection() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(CheckoutDetailsFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.popBackStack(PreselectedCheckoutMethodFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void presentDetailsActivity(@NonNull Intent intent) {
        startActivity(intent);
    }

    @Override
    public void presentDetailsFragment(@NonNull CheckoutDetailsFragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.frameLayout_fragmentContainer, fragment, CheckoutDetailsFragment.TAG)
                .addToBackStack(CheckoutDetailsFragment.TAG)
                .commit();
    }

    @Override
    public void handleWithPaymentMethodHandler(@NonNull PaymentMethodHandler paymentMethodHandler) {
        paymentMethodHandler.handlePaymentMethodDetails(this, REQUEST_CODE_PAYMENT_METHOD_DETAILS);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return;
        }

        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            cancelCheckoutActivity();
        }
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checkout);
        overridePendingTransition(0, 0);

        mCheckoutViewModel = ViewModelProviders.of(this).get(CheckoutViewModel.class);

        mBottomSheetView = findViewById(R.id.frameLayout_bottomSheet);
        mBottomSheetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                CheckoutMethodPickerFragment fragment = (CheckoutMethodPickerFragment) getSupportFragmentManager()
                        .findFragmentByTag(CheckoutMethodPickerFragment.TAG);

                if (fragment != null && fragment.isVisible()) {
                    transitionToPeekHeight(fragment.getDesiredPeekHeight());
                } else {
                    int newPeekHeight = mBottomSheetView.getMeasuredHeight();
                    transitionToPeekHeight(newPeekHeight);
                }
            }
        });

        getPaymentHandler().getPaymentSessionObservable().observe(this, new com.adyen.checkout.core.Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                mCheckoutViewModel.updateCheckoutMethodsViewModel(paymentSession);
            }
        });

        mProgressBar = findViewById(R.id.progressBar);
        ThemeUtil.applyPrimaryThemeColor(this, mProgressBar.getProgressDrawable(), mProgressBar.getIndeterminateDrawable());

        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetView);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (savedInstanceState == null) {
            // Post this, otherwise ContentLoadingProgressBar will remove the callbacks to show itself.
            mBottomSheetView.post(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.show();
                }
            });
            mCheckoutViewModel.getCheckoutMethodsLiveData().observeOnce(this, new Observer<CheckoutMethodsModel>() {
                @Override
                public void onChanged(@Nullable CheckoutMethodsModel checkoutMethodsModel) {
                    mBottomSheetView.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.hide();
                        }
                    });
                    showRequiredFragment();
                }
            });
        }

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetCallback());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PAYMENT_METHOD_DETAILS && resultCode == PaymentMethodHandler.RESULT_CODE_ERROR) {
            CheckoutException checkoutException = PaymentMethodHandler.Util.getCheckoutException(data);

            if (checkoutException != null && checkoutException.isFatal()) {
                handleCheckoutException(checkoutException);
            }
        }
    }

    @Override
    protected void handlePaymentComplete(@NonNull final PaymentResult paymentResult) {
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    CheckoutActivity.super.handlePaymentComplete(paymentResult);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        });
        mBottomSheetView.post(new Runnable() {
            @Override
            public void run() {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    private void showRequiredFragment() {
        PaymentReference paymentReference = getPaymentReference();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.executePendingTransactions();

        CheckoutMethodPickerFragment pickerFragment = CheckoutMethodPickerFragment.newInstance(paymentReference);
        fragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout_fragmentContainer, pickerFragment, CheckoutMethodPickerFragment.TAG)
                .commit();

        if (mCheckoutViewModel.getCheckoutMethodsLiveData().getPreselectedCheckoutMethod() != null) {
            PreselectedCheckoutMethodFragment fragment = PreselectedCheckoutMethodFragment.newInstance(paymentReference);
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.frameLayout_fragmentContainer, fragment, PreselectedCheckoutMethodFragment.TAG)
                    .addToBackStack(PreselectedCheckoutMethodFragment.TAG)
                    .commit();
        }
    }

    private void transitionToPeekHeight(int newPeekHeight) {
        if ((newPeekHeight <= 0) || (newPeekHeight == mTargetPeekHeight)) {
            return;
        }

        switch (mBottomSheetBehavior.getState()) {
            case BottomSheetBehavior.STATE_HIDDEN:
                mBottomSheetBehavior.setPeekHeight(newPeekHeight);
                mBottomSheetView.post(new Runnable() {
                    @Override
                    public void run() {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                });
                mTargetPeekHeight = newPeekHeight;

                break;
            case BottomSheetBehavior.STATE_COLLAPSED:
                startBottomSheetAnimation(Math.max(0, mBottomSheetBehavior.getPeekHeight()), newPeekHeight);
                mTargetPeekHeight = newPeekHeight;

                break;
            case BottomSheetBehavior.STATE_EXPANDED:
                mBottomSheetBehavior.setPeekHeight(newPeekHeight);
                mTargetPeekHeight = newPeekHeight;
                break;
            case BottomSheetBehavior.STATE_DRAGGING:
            case BottomSheetBehavior.STATE_SETTLING:
                mTargetPeekHeight = newPeekHeight;

                break;
            default:
                throw new RuntimeException("Unknown state.");
        }
    }

    private void startBottomSheetAnimation(int initialPeekHeight, int newPeekHeight) {
        cancelBottomSheetAnimation();
        mBottomSheetValueAnimator = ValueAnimator.ofInt(initialPeekHeight, newPeekHeight);
        mBottomSheetValueAnimator.setDuration(150);
        mBottomSheetValueAnimator.setInterpolator(new DecelerateInterpolator());
        mBottomSheetValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBottomSheetBehavior.setPeekHeight((int) animation.getAnimatedValue());
            }
        });
        mBottomSheetValueAnimator.start();
    }

    private void cancelBottomSheetAnimation() {
        if (mBottomSheetValueAnimator != null) {
            mBottomSheetValueAnimator.cancel();
            mBottomSheetValueAnimator = null;
        }
    }

    private void cancelCheckoutActivity() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private final class BottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_HIDDEN:
                    cancelCheckoutActivity();

                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    int peekHeight = mBottomSheetBehavior.getPeekHeight();

                    if (peekHeight != mTargetPeekHeight) {
                        startBottomSheetAnimation(peekHeight, mTargetPeekHeight);
                    }

                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    mBottomSheetBehavior.setPeekHeight(mTargetPeekHeight);

                    break;
                default:
                    // Do nothing.
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            // Nothing to do.
        }
    }
}
