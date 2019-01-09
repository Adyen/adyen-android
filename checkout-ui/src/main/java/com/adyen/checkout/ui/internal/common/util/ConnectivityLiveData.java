/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 29/03/2018.
 */

package com.adyen.checkout.ui.internal.common.util;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

public class ConnectivityLiveData extends LiveData<NetworkInfo> {
    private final Application mApplication;

    private final BroadcastReceiver mConnectivityReceiver;

    private ConnectivityManager mConnectivityManager;

    public ConnectivityLiveData(@NonNull Application application) {
        mApplication = application;
        mConnectivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateNetworkInfo();
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();

        updateNetworkInfo();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mApplication.registerReceiver(mConnectivityReceiver, filter);
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        mApplication.unregisterReceiver(mConnectivityReceiver);
    }

    private void updateNetworkInfo() {
        if (mConnectivityManager == null) {
            mConnectivityManager = ((ConnectivityManager) mApplication.getSystemService(Context.CONNECTIVITY_SERVICE));
        }

        NetworkInfo activeNetworkInfo = null;

        if (mConnectivityManager != null) {
            activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        }

        setValue(activeNetworkInfo);
    }
}
