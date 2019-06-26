/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/4/2019.
 */

package com.adyen.checkout.core.exeption;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;

public class ModelSerializationException extends CheckoutException {

    private static final long serialVersionUID = -241916181048458214L;

    public ModelSerializationException(@NonNull Class modelClass, @Nullable JSONException cause) {
        super("Unexpected exception while serializing " + modelClass.getSimpleName() + ".", cause);
    }
}
