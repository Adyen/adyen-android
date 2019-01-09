/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 30/08/2017.
 */

package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagLengthValue extends TagLength {
    private final List<TagLengthValue> mChildTagLengthValues = new ArrayList<>();

    private byte[] mValue = new byte[0];

    @Nullable
    public static TagLengthValue parseTagLengthValue(@Nullable byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        TagLengthValue tagLengthValue = new TagLengthValue();
        return tagLengthValue.parse(bytes) > 0 ? tagLengthValue : null;
    }

    @Override
    protected void clear() {
        super.clear();

        mValue = new byte[0];
    }

    @Override
    public int parse(@NonNull byte[] bytes) {
        int parsed = super.parse(bytes);

        if (parsed > 0) {
            int valueLength = getLength().intValue();
            int valueEnd = parsed + valueLength;
            mValue = Arrays.copyOfRange(bytes, parsed, valueEnd);
            setBytes(Arrays.copyOfRange(bytes, 0, valueEnd));
            parsed += valueLength;
            parseChildTagLengthValues();
        }

        return parsed;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s || Value: %s", super.toString(), ByteUtil.bytesToReadable(mValue));
    }

    @Nullable
    public TagLengthValue find(@NonNull byte[] tag) {
        return find(Tag.parseTag(tag));
    }

    @Nullable
    public TagLengthValue find(@Nullable Tag tag) {
        if (getTag().equals(tag)) {
            return this;
        } else {
            for (TagLengthValue tagLengthValue : getChildTagLengthValues()) {
                TagLengthValue result = tagLengthValue.find(tag);

                if (result != null) {
                    return result;
                }
            }

            return null;
        }
    }

    @NonNull
    public byte[] getValue() {
        return Arrays.copyOf(mValue, mValue.length);
    }

    @NonNull
    public List<TagLengthValue> getChildTagLengthValues() {
        return mChildTagLengthValues;
    }

    private void parseChildTagLengthValues() {
        mChildTagLengthValues.clear();

        int offset = 0;
        int length = mValue.length;

        while (offset < length) {
            TagLengthValue tagLengthValue = new TagLengthValue();
            int parsed = tagLengthValue.parse(Arrays.copyOfRange(mValue, offset, length));

            if (parsed > 0) {
                offset += parsed;
                mChildTagLengthValues.add(tagLengthValue);
            } else {
                break;
            }
        }

        if (!verifyChildTagLengthValues()) {
            mChildTagLengthValues.clear();
        }
    }

    private boolean verifyChildTagLengthValues() {
        int countedChildLength = 0;

        for (TagLengthValue childTagLengthValue : mChildTagLengthValues) {
            countedChildLength += childTagLengthValue.getByteCount();
        }

        return countedChildLength == mValue.length;
    }
}
