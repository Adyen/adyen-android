package com.adyen.checkout.ui.internal.common.util;

import android.arch.lifecycle.Observer;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.adyen.checkout.ui.R;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 04/04/2018.
 */
public class ConnectivityDelegate {
    private final Snackbar mSnackbar;

    private NetworkInfo mNetworkInfo;

    public ConnectivityDelegate(@NonNull AppCompatActivity activity) {
        this(activity, null);
    }

    public ConnectivityDelegate(@NonNull AppCompatActivity activity, @Nullable Observer<NetworkInfo> observer) {
        View contentView = activity.findViewById(android.R.id.content);
        mSnackbar = Snackbar.make(contentView, R.string.checkout_error_message_network_unavailable, Snackbar.LENGTH_INDEFINITE);

        ConnectivityLiveData connectivityLiveData = new ConnectivityLiveData(activity.getApplication());
        connectivityLiveData.observe(activity, new Observer<NetworkInfo>() {
            @Override
            public void onChanged(@Nullable NetworkInfo networkInfo) {
                mNetworkInfo = networkInfo;

                if (isConnectedOrConnecting()) {
                    mSnackbar.dismiss();
                } else {
                    mSnackbar.show();
                }
            }
        });

        if (observer != null) {
            connectivityLiveData.observe(activity, observer);
        }
    }

    public boolean isConnectedOrConnecting() {
        return mNetworkInfo != null && mNetworkInfo.isConnectedOrConnecting();
    }
}
