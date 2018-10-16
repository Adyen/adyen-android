package com.adyen.example.androidtest;

import android.os.IBinder;
import android.support.test.espresso.Root;
import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by Ran Haveshush on 10/10/2018.
 */
public class ToastMatcher extends TypeSafeMatcher<Root> {
    @Override
    protected boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;

        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder applicationWindowToken = root.getDecorView().getApplicationWindowToken();
            return windowToken == applicationWindowToken;
        }

        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }
}
