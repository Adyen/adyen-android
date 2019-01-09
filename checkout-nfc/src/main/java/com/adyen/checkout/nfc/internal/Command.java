/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 30/08/2017.
 */

package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;

import java.util.Arrays;

@SuppressWarnings("checkstyle:MagicNumber")
public final class Command {
    private final byte mCla;

    private final byte mIns;

    private final byte mP1;

    private final byte mP2;

    private final byte[] mLength;

    private final byte[] mData;

    private final byte[] mMaxResponseLength;

    public Command(byte cla, byte ins, byte p1, byte p2, @NonNull byte[] length, @NonNull byte[] data, @NonNull byte[] maxResponseLength) {
        mCla = cla;
        mIns = ins;
        mP1 = p1;
        mP2 = p2;
        mLength = Arrays.copyOf(length, length.length);
        mData = Arrays.copyOf(data, data.length);
        mMaxResponseLength = Arrays.copyOf(maxResponseLength, maxResponseLength.length);
    }

    @NonNull
    public static Command select(@NonNull byte... data) {
        byte[] length = getLengthBytes(data.length);
        byte[] responseLength = getResponseLengthBytes();

        return new Command((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, length, data, responseLength);
    }

    @NonNull
    public static Command read(byte p1, byte p2, @NonNull byte... data) {
        return read(p1, p2, data, getResponseLengthBytes());
    }

    @NonNull
    public static Command read(byte p1, byte p2, @NonNull byte[] data, @NonNull byte... responseLength) {
        byte[] length = getLengthBytes(data.length);

        return new Command((byte) 0x00, (byte) 0xB2, p1, p2, length, data, responseLength);
    }

    @NonNull
    public static Command getProcessingOptions(@NonNull byte... data) {
        byte[] length = getLengthBytes(data.length);
        byte[] responseLength = getResponseLengthBytes();

        return new Command((byte) 0x80, (byte) 0xA8, (byte) 0x00, (byte) 0x00, length, data, responseLength);
    }

    @NonNull
    public static Command getData(@NonNull byte... data) {
        byte[] length = getLengthBytes(data.length);
        byte[] responseLength = getResponseLengthBytes();

        return new Command((byte) 0x80, (byte) 0xCA, (byte) 0x00, (byte) 0x00, length, data, responseLength);
    }

    @NonNull
    private static byte[] getLengthBytes(int length) {
        if (length == 0) {
            return new byte[0];
        } else if (length < 256) {
            return new byte[] {(byte) (length)};
        } else if (length < 65_536) {
            return new byte[] {(byte) (length >>> 16), (byte) (length >>> 8), (byte) (length)};
        } else {
            throw new IllegalArgumentException("Invalid length");
        }
    }

    @NonNull
    private static byte[] getResponseLengthBytes() {
        return new byte[] {(byte) 0x00};
    }

    @NonNull
    @Override
    public String toString() {
        return ByteUtil.bytesToHexFormatted(getBytes());
    }

    @NonNull
    public byte[] getBytes() {
        byte[] bytes = new byte[1 + 1 + 1 + 1 + Math.max(mLength.length, 1) + mData.length + mMaxResponseLength.length];
        bytes[0] = mCla;
        bytes[1] = mIns;
        bytes[2] = mP1;
        bytes[3] = mP2;

        int index = 4;
        int length = mLength.length;

        if (length > 0) {
            System.arraycopy(mLength, 0, bytes, index, length);
        } else {
            bytes[index] = 0;
            length++;
        }

        index += length;
        length = mData.length;
        System.arraycopy(mData, 0, bytes, index, length);

        index += length;
        length = mMaxResponseLength.length;
        System.arraycopy(mMaxResponseLength, 0, bytes, index, length);

        return bytes;
    }
}
