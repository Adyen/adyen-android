package com.adyen.checkout.core.internal.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.internal.model.PaymentSessionImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 29/05/2018.
 */
@Entity(tableName = "payment_sessions")
public final class PaymentSessionEntity {
    @NonNull
    @PrimaryKey()
    @ColumnInfo(name = "uuid")
    public String uuid;

    @NonNull
    @TypeConverters(PaymentSessionConverter.class)
    @ColumnInfo(name = "payment_session_json")
    public PaymentSessionImpl paymentSession;

    @NonNull
    @ColumnInfo(name = "generation_timestamp")
    public Date generationTime;

    public static final class PaymentSessionConverter {
        @TypeConverter
        @NonNull
        public String fromPaymentSession(@NonNull PaymentSessionImpl paymentSession) {
            try {
                return JsonObject.serialize(paymentSession).toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @TypeConverter
        @NonNull
        public PaymentSessionImpl toPaymentSession(@NonNull String paymentSessionJson) {
            try {
                JSONObject jsonObject = new JSONObject(paymentSessionJson);

                return JsonObject.parseFrom(jsonObject, PaymentSessionImpl.class);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
