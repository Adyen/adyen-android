/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.core.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;

/**
 * Exception thrown when an issue occurs during serialization of a {@link ModelObject}.
 */
public class ModelSerializationException extends CheckoutException {

    private static final long serialVersionUID = -241916181048458214L;

    public ModelSerializationException(@NonNull Class modelClass, @Nullable JSONException cause) {
        super("Unexpected exception while serializing " + modelClass.getSimpleName() + ".", cause);
    }
}
