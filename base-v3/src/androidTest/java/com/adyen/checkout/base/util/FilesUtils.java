/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 13/02/2019.
 */

package com.adyen.checkout.base.util;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FilesUtils {

    @Nullable
    public static String read(@NonNull Context context, @NonNull String fileName) {
        InputStream inputStream = null;
        Scanner scanner = null;

        try {
            AssetManager assetManager = context.getAssets();
            inputStream = assetManager.open(fileName);
            scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            return scanner.next();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // Do nothing.
            }

            if (scanner != null) {
                scanner.close();
            }
        }

        return null;
    }

    private FilesUtils() {
        throw new NoConstructorException();
    }
}
