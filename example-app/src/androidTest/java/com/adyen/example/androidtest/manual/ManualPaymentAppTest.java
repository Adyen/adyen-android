package com.adyen.example.androidtest.manual;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;

import com.adyen.checkout.util.PaymentMethodTypes;
import com.adyen.example.MainActivity;
import com.adyen.example.androidtest.EspressoTestUtils;
import com.adyen.example.androidtest.PaymentSetup;
import com.adyen.example.model.PaymentVerifyResponse;

import org.junit.After;
import org.junit.Rule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static com.adyen.example.androidtest.EspressoTestUtils.waitForToastWithText;
import static com.adyen.example.androidtest.PaymentAppTestUtils.changePaymentSetup;
import static com.adyen.example.androidtest.PaymentAppTestUtils.confirmWeChatPayDetailsDisplayed;
import static com.adyen.example.androidtest.PaymentAppTestUtils.goToPaymentMethodsOverview;
import static com.adyen.example.androidtest.PaymentAppTestUtils.selectPaymentMethodByType;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by Ran Haveshush on 13/10/2017.
 */
//@RunWith(AndroidJUnit4.class)
public class ManualPaymentAppTest {
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @After
    public void tearDown() throws Exception {
        EspressoTestUtils.closeAllActivities(getInstrumentation());
    }

    /**
     * This test case tests the WeChat Pay SDK payment flow.
     * In order for this test to pass the WeChat app should be already installed on the device.
     *
     * @throws Exception
     */
//    @Test
    public void testWeChatPayPayment() throws Exception {
        PaymentSetup paymentSetup = new PaymentSetup.Builder()
                .setAmount(new PaymentSetup.Amount(500L, "CNY"))
                .setCountryCode("CN")
                .build();

        changePaymentSetup(paymentSetup);

        goToPaymentMethodsOverview();

        selectPaymentMethodByType(PaymentMethodTypes.WECHAT_PAY_SDK);

        confirmWeChatPayDetailsDisplayed();

        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        mDevice.pressBack();

        Thread.sleep(500);

        mDevice.pressBack();

        waitForToastWithText(PaymentVerifyResponse.ResultCode.CANCELLED.name());
    }
}
