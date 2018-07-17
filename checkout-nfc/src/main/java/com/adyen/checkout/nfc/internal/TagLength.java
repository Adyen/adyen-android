package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 07/09/2017.
 */
public class TagLength extends ByteParsable {
    private final Tag mTag = new Tag();

    private final Length mLength = new Length();

    @Nullable
    public static TagLength parseTagLength(@NonNull byte[] bytes) {
        TagLength tagLength = new TagLength();
        return tagLength.parse(bytes) > 0 ? tagLength : null;
    }

    @Override
    public int parse(@NonNull byte[] bytes) {
        int lengthStart = mTag.parse(bytes);

        if (lengthStart > 0) {
            int lengthEnd = lengthStart + mLength.parse(Arrays.copyOfRange(bytes, lengthStart, bytes.length));

            if (lengthEnd > lengthStart) {
                setBytes(Arrays.copyOfRange(bytes, 0, lengthEnd));
                return lengthEnd;
            }
        }

        mTag.clear();
        mLength.clear();

        return 0;
    }

    @Override
    public String toString() {
        return String.format("Tag: %s || Length: %s", mTag.toString(), mLength.toString());
    }

    @NonNull
    public Tag getTag() {
        return mTag;
    }

    @NonNull
    public Length getLength() {
        return mLength;
    }
}
