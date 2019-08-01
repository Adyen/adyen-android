/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public interface ActionComponentProvider<ComponentT extends ActionComponent> extends ComponentProvider<ComponentT> {

    @NonNull
    ComponentT get(@NonNull FragmentActivity activity);

    @NonNull
    ComponentT get(@NonNull Fragment fragment);
}
