/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 10/07/2018.
 */

package com.adyen.checkout.core.internal;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.CheckoutException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProvidedBy {
    @NonNull
    Class<? extends JsonObject> value();

    final class Util {
        @NonNull
        public static <T> T parse(@NonNull JSONObject jsonObject, @NonNull Class<T> clazz) throws CheckoutException {
            try {
                ProvidedBy providerBy = clazz.getAnnotation(ProvidedBy.class);
                Class<? extends JsonObject> providerClass = providerBy.value();

                //noinspection unchecked
                return (T) JsonObject.parseFrom(jsonObject, providerClass);
            } catch (Exception e) {
                if (e instanceof JSONException) {
                    throw new CheckoutException.Builder("Data does not match fields of " + clazz.getSimpleName() + ".", e).build();
                } else {
                    throw new RuntimeException(clazz.getSimpleName() + " could not be parsed.", e);
                }
            }
        }

        private Util() {
            throw new IllegalStateException("No instances.");
        }
    }
}
