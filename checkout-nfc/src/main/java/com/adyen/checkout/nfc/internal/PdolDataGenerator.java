package com.adyen.checkout.nfc.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 07/09/2017.
 */
public class PdolDataGenerator extends ByteParsable {
    private static final Random RANDOM = new Random();

    @NonNull
    public static PdolDataGenerator create(@Nullable TagLengthValue pdolOrParent) {
        PdolDataGenerator pdolDataGenerator = new PdolDataGenerator();
        TagLengthValue pdol = pdolOrParent != null ? pdolOrParent.find(Tags.PDOL) : null;
        byte[] bytes = pdol != null ? pdol.getValue() : new byte[0];
        pdolDataGenerator.parse(bytes);

        return pdolDataGenerator;
    }

    @Override
    public int parse(@NonNull byte[] bytes) {
        List<TagLength> tagLengths = Parser.parseList(bytes, TagLength.class);

        int index = 0;
        byte length = getLength(tagLengths);

        byte[] result = new byte[2 + length];
        result[index++] = (byte) 0x83;
        result[index++] = length;

        for (TagLength tagLength : tagLengths) {
            byte[] element = constructElement(tagLength);
            int elementLength = element.length;
            System.arraycopy(element, 0, result, index, elementLength);
            index += elementLength;
        }

        setBytes(result);

        return bytes.length;
    }

    private byte getLength(@NonNull List<TagLength> tagLengths) {
        byte length = 0;

        for (TagLength tagLength : tagLengths) {
            length += tagLength.getLength().intValue();
        }

        return length;
    }

    @NonNull
    private byte[] constructElement(@NonNull TagLength tagLength) {
        byte[] result = new byte[tagLength.getLength().intValue()];
        byte[] value = null;

        if (tagLength.getTag().equals(Tags.TERMINAL_TRANSACTION_QUALIFIERS)) {
            value = new byte[] {(byte) 0x36, (byte) 0x20, (byte) 0x40, (byte) 0x00}; // Contactless, Offline only
        } else if (tagLength.getTag().equals(Tags.TERMINAL_COUNTRY_CODE)) {
            value = new byte[] {0x05, 0x28}; // NL = 528
        } else if (tagLength.getTag().equals(Tags.TRANSACTION_CURRENCY_CODE)) {
            value = new byte[] {0x08, 0x40}; // USD
        } else if (tagLength.getTag().equals(Tags.TRANSACTION_DATE)) {
            value = new byte[] {0x17, 0x09, 0x07}; // yy MM dd
        } else if (tagLength.getTag().equals(Tags.TRANSACTION_TYPE)) {
            value = new byte[] {(byte) 0x00}; // Purchase
        } else if (tagLength.getTag().equals(Tags.AMOUNT_AUTHORIZED_NUMERIC)) {
            value = new byte[] {0x00}; // 0.00
        } else if (tagLength.getTag().equals(Tags.TERMINAL_TYPE)) {
            value = new byte[] {0x22};
        } else if (tagLength.getTag().equals(Tags.TERMINAL_CAPABILITIES)) {
            value = new byte[] {(byte) 0xE0, (byte) 0xA0, 0x00};
        } else if (tagLength.getTag().equals(Tags.ADDITIONAL_TERMINAL_CAPABILITIES)) {
            value = new byte[] {(byte) 0x8E, (byte) 0x00, (byte) 0xB0, 0x50, 0x05};
        } else if (tagLength.getTag().equals(Tags.DS_REQUESTED_OPERATOR_ID)) {
            value = new byte[] {(byte) 0x73, (byte) 0x45, (byte) 0x12, (byte) 0x32, (byte) 0x15, (byte) 0x90, (byte) 0x45, (byte) 0x01};
        } else if (tagLength.getTag().equals(Tags.UNPREDICTABLE_NUMBER)) {
            RANDOM.nextBytes(result);
        }

        if (value != null) {
            System.arraycopy(value, 0, result, 0, Math.min(value.length, result.length));
        }

        return result;
    }
}
