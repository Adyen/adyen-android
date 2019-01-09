/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 29/05/2018.
 */

package com.adyen.checkout.core.internal.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.internal.model.PaymentInitiationResponse;

import org.json.JSONException;
import org.json.JSONObject;

@Entity(tableName = "payment_initiation_responses",
        indices = {@Index("payment_session_uuid")},
        foreignKeys = @ForeignKey(
                entity = PaymentSessionEntity.class,
                parentColumns = "uuid",
                childColumns = "payment_session_uuid",
                onUpdate = ForeignKey.CASCADE,
                onDelete = ForeignKey.CASCADE
        ))
@SuppressWarnings("MemberName")
public final class PaymentInitiationResponseEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id;

    @NonNull
    @ColumnInfo(name = "payment_session_uuid")
    public String paymentSessionUuid;

    @NonNull
    @ColumnInfo(name = "payment_method_json")
    @TypeConverters(PaymentMethodConverter.class)
    public PaymentMethodImpl paymentMethod;

    @NonNull
    @ColumnInfo(name = "payment_initiation_response_json")
    @TypeConverters(PaymentInitiationResponseConverter.class)
    public PaymentInitiationResponse paymentInitiationResponse;

    @ColumnInfo(name = "handled")
    public boolean handled;

    public static final class PaymentMethodConverter {
        @TypeConverter
        @NonNull
        public String fromPaymentMethod(@NonNull PaymentMethodImpl paymentMethod) {
            try {
                return JsonObject.serialize(paymentMethod).toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @TypeConverter
        @NonNull
        public PaymentMethodImpl toPaymentMethod(@NonNull String paymentMethodJson) {
            try {
                JSONObject jsonObject = new JSONObject(paymentMethodJson);

                return JsonObject.parseFrom(jsonObject, PaymentMethodImpl.class);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final class PaymentInitiationResponseConverter {
        @TypeConverter
        @NonNull
        public String fromPaymentInitiationResponse(@NonNull PaymentInitiationResponse paymentInitiationResponse) {
            return JsonObject.serialize(paymentInitiationResponse).toString();
        }

        @TypeConverter
        @NonNull
        public PaymentInitiationResponse toPaymentInitiationResponse(@NonNull String paymentInitiationResponseJson) {
            try {
                JSONObject jsonObject = new JSONObject(paymentInitiationResponseJson);

                return JsonObject.parseFrom(jsonObject, PaymentInitiationResponse.class);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
