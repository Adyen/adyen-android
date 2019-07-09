/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.base.component;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.adyen.checkout.base.ActionComponentProvider;

public class ActionComponentProviderImpl<ComponentT extends BaseActionComponent> implements ActionComponentProvider<ComponentT> {

    private final Class<ComponentT> mComponentClass;

    public ActionComponentProviderImpl(@NonNull Class<ComponentT> componentClass) {
        mComponentClass = componentClass;
    }

    @NonNull
    @Override
    public ComponentT get(@NonNull FragmentActivity activity) {
        return ViewModelProviders.of(activity).get(mComponentClass);
    }

    @NonNull
    @Override
    public ComponentT get(@NonNull Fragment fragment) {
        return ViewModelProviders.of(fragment).get(mComponentClass);
    }
}
