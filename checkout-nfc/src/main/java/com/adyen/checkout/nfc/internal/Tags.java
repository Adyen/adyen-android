package com.adyen.checkout.nfc.internal;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 05/09/2017.
 */
public final class Tags {
    public static final Tag APPLICATION_ID = Tag.parseTag((byte) 0x4F);

    public static final Tag APPLICATION_LABEL = Tag.parseTag((byte) 0x50);

    public static final Tag APPLICATION_PREFERRED_NAME = Tag.parseTag((byte) 0x9F, (byte) 0x12);

    public static final Tag APPLICATION_PRIORITY_INDICATOR = Tag.parseTag((byte) 0x87);

    public static final Tag APPLICATION_DATA_FILE = Tag.parseTag((byte) 0x61);

    public static final Tag APPLICATION_FILE_LOCATOR = Tag.parseTag((byte) 0x94);

    public static final Tag FCI_PROPRIETARY_TEMPLATE = Tag.parseTag((byte) 0xA5);

    public static final Tag SFI = Tag.parseTag((byte) 0x88);

    public static final Tag LANGUAGE_PREFERENCE = Tag.parseTag((byte) 0x5F, (byte) 0x2D);

    public static final Tag FCI_TEMPLATE = Tag.parseTag((byte) 0x6F);

    public static final Tag DF_NAME = Tag.parseTag((byte) 0x84);

    public static final Tag TRACK_TWO_EQUIVALENT_DATA = Tag.parseTag((byte) 0x57);

    public static final Tag PDOL = Tag.parseTag((byte) 0x9F, (byte) 0x38);

    public static final Tag PSE_RECORD = Tag.parseTag((byte) 0x70);

    public static final Tag APPLICATION_EXPIRATION_DATE = Tag.parseTag((byte) 0x5F, (byte) 0x24);

    public static final Tag APPLICATION_PRIMARY_ACCOUNT_NUMBER = Tag.parseTag((byte) 0x5A);

    public static final Tag CARDHOLDER_NAME = Tag.parseTag((byte) 0x5F, (byte) 0x20);

    public static final Tag DATA_OBJECT_FORMAT_1 = Tag.parseTag((byte) 0x77);

    public static final Tag DATA_OBJECT_FORMAT_2 = Tag.parseTag((byte) 0x80);

    // ############################################## PDOL ############################################## //

    public static final Tag TERMINAL_TRANSACTION_QUALIFIERS = Tag.parseTag((byte) 0x9F, (byte) 0x66);

    public static final Tag TERMINAL_COUNTRY_CODE = Tag.parseTag((byte) 0x9F, (byte) 0x1A);

    public static final Tag TRANSACTION_CURRENCY_CODE = Tag.parseTag((byte) 0x5F, (byte) 0x2A);

    public static final Tag TRANSACTION_DATE = Tag.parseTag((byte) 0x9A);

    public static final Tag TRANSACTION_TYPE = Tag.parseTag((byte) 0x9c);

    public static final Tag AMOUNT_AUTHORIZED_NUMERIC = Tag.parseTag((byte) 0x9F, (byte) 0x02);

    public static final Tag TERMINAL_TYPE = Tag.parseTag((byte) 0x9F, (byte) 0x35);

    public static final Tag TERMINAL_CAPABILITIES = Tag.parseTag((byte) 0x9F, (byte) 0x33);

    public static final Tag ADDITIONAL_TERMINAL_CAPABILITIES = Tag.parseTag((byte) 0x9F, (byte) 0x40);

    public static final Tag DS_REQUESTED_OPERATOR_ID = Tag.parseTag((byte) 0x9F, (byte) 0x5C);

    public static final Tag UNPREDICTABLE_NUMBER = Tag.parseTag((byte) 0x9F, (byte) 0x37);

    private Tags() {
        throw new IllegalStateException("No instances.");
    }
}
