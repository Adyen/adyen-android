/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 07/08/2017.
 */

package com.adyen.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentResult;
import com.adyen.checkout.core.StartPaymentParameters;
import com.adyen.checkout.core.handler.StartPaymentParametersHandler;
import com.adyen.checkout.ui.CheckoutController;
import com.adyen.checkout.ui.CheckoutSetupParameters;
import com.adyen.checkout.ui.CheckoutSetupParametersHandler;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;
import com.adyen.example.model.PaymentSetupRequest;
import com.adyen.example.model.PaymentVerifyRequest;
import com.adyen.example.model.PaymentVerifyResponse;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_CHECKOUT = 1;

    private ViewPager mViewPager;

    private Button mCheckoutButton;

    private ContentLoadingProgressBar mProgressBar;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private TabsAdapter mTabsAdapter;

    private ViewPager.SimpleOnPageChangeListener mPageChangeListener;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_checkout) {
            CheckoutController.startPayment(this, new CheckoutSetupParametersHandler() {
                @Override
                public void onRequestPaymentSession(@NonNull CheckoutSetupParameters checkoutSetupParameters) {
                    retrievePaymentSession(checkoutSetupParameters);
                }

                @Override
                public void onError(@NonNull CheckoutException checkoutException) {
                    handleCheckoutException(checkoutException);
                }
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTabsAdapter = new TabsAdapter(getSupportFragmentManager());

        mPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == TabsAdapter.FRAGMENT_POSITION_SHOPPING_CART) {
                    KeyboardUtil.hide(mViewPager);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                } else if (position == TabsAdapter.FRAGMENT_POSITION_CONFIGURATION) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
            }
        };

        mViewPager = findViewById(R.id.viewPager_tabs);
        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mProgressBar = findViewById(R.id.progressBar);
        mCheckoutButton = findViewById(R.id.button_checkout);
        mCheckoutButton.setOnClickListener(this);

        if (savedInstanceState == null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CHECKOUT) {
            if (resultCode == PaymentMethodHandler.RESULT_CODE_OK) {
                PaymentResult paymentResult = PaymentMethodHandler.Util.getPaymentResult(data);
                //noinspection ConstantConditions
                verify(paymentResult.getPayload());
            } else {
                CheckoutException checkoutException = PaymentMethodHandler.Util.getCheckoutException(data);
                String message = checkoutException != null ? checkoutException.getMessage() : "null";

                if (resultCode == PaymentMethodHandler.RESULT_CODE_CANCELED) {
                    Toast.makeText(MainActivity.this, "Cancelled. Error: " + message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }

            mCheckoutButton.setClickable(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCompositeDisposable.dispose();
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
    }

    private void retrievePaymentSession(@NonNull CheckoutSetupParameters checkoutSetupParameters) {
        ConfigurationFragment configurationFragment = mTabsAdapter.getConfigurationFragment();
        PaymentSetupRequest paymentSetupRequest = configurationFragment != null
                ? configurationFragment.getPaymentSetupRequest(checkoutSetupParameters)
                : null;

        if (paymentSetupRequest == null) {
            mViewPager.setCurrentItem(TabsAdapter.FRAGMENT_POSITION_CONFIGURATION);
        } else {
            mCheckoutButton.setClickable(false);
            mProgressBar.show();

            mViewPager.setCurrentItem(TabsAdapter.FRAGMENT_POSITION_SHOPPING_CART);

            CheckoutService.INSTANCE
                    .paymentSession(paymentSetupRequest)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(createPaymentSessionObserver());
        }
    }

    private void handleCheckoutException(@NonNull CheckoutException checkoutException) {
        Toast.makeText(MainActivity.this, "Error: " + checkoutException.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void verify(@NonNull String payload) {
        mProgressBar.show();
        PaymentVerifyRequest paymentVerifyRequest = new PaymentVerifyRequest(payload);

        CheckoutService.INSTANCE
                .verify(paymentVerifyRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createVerifyObserver());
    }

    @NonNull
    private Observer<ResponseBody> createPaymentSessionObserver() {
        return new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                mCompositeDisposable.add(disposable);
            }

            @Override
            public void onNext(@NonNull ResponseBody responseBody) {
                try {
                    String encodedPaymentSession = responseBody.string();
                    StartPaymentParametersHandler handler = createStartPaymentParametersHandler();
                    CheckoutController.handlePaymentSessionResponse(MainActivity.this, encodedPaymentSession, handler);
                } catch (IOException | IllegalArgumentException e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, e.getLocalizedMessage(), e);
                    mCheckoutButton.setClickable(true);
                }
            }

            @Override
            public void onError(@NonNull Throwable error) {
                Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.hide();
                mCheckoutButton.setClickable(true);
            }

            @Override
            public void onComplete() {
                mProgressBar.hide();
            }
        };
    }

    @NonNull
    private StartPaymentParametersHandler createStartPaymentParametersHandler() {
        return new StartPaymentParametersHandler() {
            @Override
            public void onPaymentInitialized(@NonNull StartPaymentParameters startPaymentParameters) {
                PaymentMethodHandler checkoutHandler = CheckoutController.getCheckoutHandler(startPaymentParameters);
                checkoutHandler.handlePaymentMethodDetails(MainActivity.this, REQUEST_CODE_CHECKOUT);
            }

            @Override
            public void onError(@NonNull CheckoutException checkoutException) {
                handleCheckoutException(checkoutException);
            }
        };
    }

    @NonNull
    private Observer<PaymentVerifyResponse> createVerifyObserver() {
        return new Observer<PaymentVerifyResponse>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
                mCompositeDisposable.add(disposable);
            }

            @Override
            public void onNext(@NonNull PaymentVerifyResponse paymentVerifyResponse) {
                PaymentVerifyResponse.ResultCode resultCode = paymentVerifyResponse.getResultCode();

                if (resultCode == PaymentVerifyResponse.ResultCode.AUTHORIZED || resultCode == PaymentVerifyResponse.ResultCode.RECEIVED) {
                    startActivity(new Intent(MainActivity.this, SuccessActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Result: " + resultCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull Throwable error) {
                Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.hide();
            }

            @Override
            public void onComplete() {
                mProgressBar.hide();
            }
        };
    }
}
