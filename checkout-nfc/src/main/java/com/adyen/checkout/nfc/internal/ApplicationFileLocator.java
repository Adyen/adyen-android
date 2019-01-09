/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 07/09/2017.
 */

package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

@SuppressWarnings("checkstyle:MagicNumber")
public class ApplicationFileLocator extends ByteParsable {
    private static final int LENGTH = 4;

    @Nullable
    public static ApplicationFileLocator parseApplicationFileLocator(@NonNull byte[] bytes) {
        ApplicationFileLocator applicationFileLocator = new ApplicationFileLocator();
        return applicationFileLocator.parse(bytes) == LENGTH ? applicationFileLocator : null;
    }

    @Override
    public int parse(@NonNull byte[] bytes) {
        if (bytes.length >= LENGTH) {
            setBytes(Arrays.copyOfRange(bytes, 0, LENGTH));
            return LENGTH;
        }

        return 0;
    }

    public byte getSfi() {
        return (byte) (getBytes()[0] >> 3);
    }

    public byte getFirstRecordIndex() {
        return getBytes()[1];
    }

    public byte getLastRecordIndex() {
        return getBytes()[2];
    }

    public boolean isOfflineDataAuthentication() {
        return getBytes()[3] == 1;
    }
}
