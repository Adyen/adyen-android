package com.adyen.checkout.core.internal.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 29/05/2018.
 */
@Database(entities = {PaymentSessionEntity.class, PaymentInitiationResponseEntity.class}, version = 1, exportSchema = false)
@TypeConverters(PaymentDatabase.DateConverter.class)
abstract class PaymentDatabase extends RoomDatabase {
    @NonNull
    public abstract PaymentSessionDao getPaymentSessionDao();

    @NonNull
    public abstract PaymentInitiationResponseDao getPaymentInitiationResponseDao();

    public static final class DateConverter {
        @TypeConverter
        @NonNull
        public static Date fromTimestamp(long timestamp) {
            return new Date(timestamp);
        }

        @TypeConverter
        @NonNull
        public static long toTimestamp(@NonNull Date date) {
            return date.getTime();
        }
    }
}
