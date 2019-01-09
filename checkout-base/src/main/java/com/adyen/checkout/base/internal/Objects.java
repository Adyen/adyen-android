/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 24/04/2018.
 */

package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public final class Objects {
    @NonNull
    public static <T> T requireNonNull(@Nullable T t, @NonNull String message) {
        if (t == null) {
            throw new NullPointerException(message);
        } else {
            return t;
        }
    }

    @NonNull
    public static <T> T reflectiveInit(@NonNull Class<T> clazz, @NonNull Object... arguments) throws InvocationTargetException {
        Class<?>[] argumentTypes = new Class<?>[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            argumentTypes[i] = arguments[i].getClass();
        }

        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(argumentTypes);

            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }

            return constructor.newInstance(arguments);
        } catch (NoSuchMethodException e) {
            String argsString = Arrays.toString(argumentTypes);

            throw new RuntimeException("Class " + clazz.getSimpleName() + " does not declare constructor with arguments " + argsString + ".", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Constructor of " + clazz.getSimpleName() + " could not be made accessible.", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Class " + clazz.getSimpleName() + " could not be instantiated.", e);
        }
    }

    private Objects() {
        throw new IllegalStateException("No instances.");
    }
}
