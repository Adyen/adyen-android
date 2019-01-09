/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 05/09/2017.
 */

package com.adyen.checkout.nfc.internal;

import android.support.annotation.Nullable;

public final class Tags {
    @Nullable
    public static final Tag APPLICATION_ID = Tag.parseTag((byte) 0x4F);

    @Nullable
    public static final Tag APPLICATION_LABEL = Tag.parseTag((byte) 0x50);

    @Nullable
    public static final Tag APPLICATION_PREFERRED_NAME = Tag.parseTag((byte) 0x9F, (byte) 0x12);

    @Nullable
    public static final Tag APPLICATION_PRIORITY_INDICATOR = Tag.parseTag((byte) 0x87);

    @Nullable
    public static final Tag APPLICATION_DATA_FILE = Tag.parseTag((byte) 0x61);

    @Nullable
    public static final Tag APPLICATION_FILE_LOCATOR = Tag.parseTag((byte) 0x94);

    @Nullable
    public static final Tag FCI_PROPRIETARY_TEMPLATE = Tag.parseTag((byte) 0xA5);

    @Nullable
    public static final Tag SFI = Tag.parseTag((byte) 0x88);

    @Nullable
    public static final Tag LANGUAGE_PREFERENCE = Tag.parseTag((byte) 0x5F, (byte) 0x2D);

    @Nullable
    public static final Tag FCI_TEMPLATE = Tag.parseTag((byte) 0x6F);

    @Nullable
    public static final Tag DF_NAME = Tag.parseTag((byte) 0x84);

    @Nullable
    public static final Tag TRACK_TWO_EQUIVALENT_DATA = Tag.parseTag((byte) 0x57);

    @Nullable
    public static final Tag PDOL = Tag.parseTag((byte) 0x9F, (byte) 0x38);

    @Nullable
    public static final Tag PSE_RECORD = Tag.parseTag((byte) 0x70);

    @Nullable
    public static final Tag APPLICATION_EXPIRATION_DATE = Tag.parseTag((byte) 0x5F, (byte) 0x24);

    @Nullable
    public static final Tag APPLICATION_PRIMARY_ACCOUNT_NUMBER = Tag.parseTag((byte) 0x5A);

    @Nullable
    public static final Tag CARDHOLDER_NAME = Tag.parseTag((byte) 0x5F, (byte) 0x20);

    @Nullable
    public static final Tag DATA_OBJECT_FORMAT_1 = Tag.parseTag((byte) 0x77);

    @Nullable
    public static final Tag DATA_OBJECT_FORMAT_2 = Tag.parseTag((byte) 0x80);

    // ############################################## PDOL ############################################## //

    @Nullable
    public static final Tag TERMINAL_TRANSACTION_QUALIFIERS = Tag.parseTag((byte) 0x9F, (byte) 0x66);

    @Nullable
    public static final Tag TERMINAL_COUNTRY_CODE = Tag.parseTag((byte) 0x9F, (byte) 0x1A);

    @Nullable
    public static final Tag TRANSACTION_CURRENCY_CODE = Tag.parseTag((byte) 0x5F, (byte) 0x2A);

    @Nullable
    public static final Tag TRANSACTION_DATE = Tag.parseTag((byte) 0x9A);

    @Nullable
    public static final Tag TRANSACTION_TYPE = Tag.parseTag((byte) 0x9c);

    @Nullable
    public static final Tag AMOUNT_AUTHORIZED_NUMERIC = Tag.parseTag((byte) 0x9F, (byte) 0x02);

    @Nullable
    public static final Tag TERMINAL_TYPE = Tag.parseTag((byte) 0x9F, (byte) 0x35);

    @Nullable
    public static final Tag TERMINAL_CAPABILITIES = Tag.parseTag((byte) 0x9F, (byte) 0x33);

    @Nullable
    public static final Tag ADDITIONAL_TERMINAL_CAPABILITIES = Tag.parseTag((byte) 0x9F, (byte) 0x40);

    @Nullable
    public static final Tag DS_REQUESTED_OPERATOR_ID = Tag.parseTag((byte) 0x9F, (byte) 0x5C);

    @Nullable
    public static final Tag UNPREDICTABLE_NUMBER = Tag.parseTag((byte) 0x9F, (byte) 0x37);

    private Tags() {
        throw new IllegalStateException("No instances.");
    }
}
