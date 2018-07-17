package com.adyen.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 07/08/2017.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_CHECKOUT = 1;

    private static final int FRAGMENT_POSITION_SHOPPING_CART = 0;

    private static final int FRAGMENT_POSITION_CONFIGURATION = FRAGMENT_POSITION_SHOPPING_CART + 1;

    private static final int FRAGMENT_COUNT = FRAGMENT_POSITION_CONFIGURATION + 1;

    private ConfigurationFragment mConfigurationFragment;

    private ViewPager mViewPager;

    private Button mCheckoutButton;

    private ContentLoadingProgressBar mProgressBar;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

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

        mViewPager = findViewById(R.id.viewPager_tabs);
        mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == FRAGMENT_POSITION_SHOPPING_CART) {
                    KeyboardUtil.hide(mViewPager);
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                } else if (position == FRAGMENT_POSITION_CONFIGURATION) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
            }
        });
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
    }

    private void retrievePaymentSession(@NonNull CheckoutSetupParameters checkoutSetupParameters) {
        PaymentSetupRequest paymentSetupRequest = mConfigurationFragment != null
                ? mConfigurationFragment.getPaymentSetupRequest(checkoutSetupParameters)
                : null;

        if (paymentSetupRequest == null) {
            mViewPager.setCurrentItem(FRAGMENT_POSITION_CONFIGURATION);
        } else {
            mCheckoutButton.setClickable(false);
            mProgressBar.show();

            mViewPager.setCurrentItem(FRAGMENT_POSITION_SHOPPING_CART);

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
                PaymentVerifyResponse.AuthResponse authResponse = paymentVerifyResponse.getAuthResponse();

                if (authResponse == PaymentVerifyResponse.AuthResponse.AUTHORIZED || authResponse == PaymentVerifyResponse.AuthResponse.RECEIVED) {
                    startActivity(new Intent(MainActivity.this, SuccessActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Result: " + authResponse, Toast.LENGTH_SHORT).show();
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

    private final class TabsAdapter extends FragmentPagerAdapter {
        private TabsAdapter(@NonNull FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return FRAGMENT_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case FRAGMENT_POSITION_SHOPPING_CART:
                    return new ShoppingCartFragment();
                case FRAGMENT_POSITION_CONFIGURATION:
                    return new ConfigurationFragment();
                default:
                    throw new IllegalArgumentException("Invalid position.");
            }
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);

            if (position == FRAGMENT_POSITION_CONFIGURATION) {
                mConfigurationFragment = (ConfigurationFragment) fragment;
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);

            if (object == mConfigurationFragment) {
                mConfigurationFragment = null;
            }
        }
    }
}
