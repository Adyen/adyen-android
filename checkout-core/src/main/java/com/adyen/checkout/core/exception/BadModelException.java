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
import com.adyen.checkout.core.model.ModelUtils;

/**+
 * Exception thrown when a {@link ModelObject} does not meet the requirement of having a SERIALIZER object.
 */
public class BadModelException extends CheckoutException {

    private static final long serialVersionUID = -1161500360463809921L;

    public BadModelException(@NonNull Class<?> clazz, @Nullable Throwable e) {
        super("ModelObject protocol requires a ModelObject.Serializer object called " + ModelUtils.SERIALIZER_FIELD_NAME + " on class "
                + clazz.getSimpleName(), e);
    }
}
