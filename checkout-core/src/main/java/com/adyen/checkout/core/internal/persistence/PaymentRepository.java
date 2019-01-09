/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 29/05/2018.
 */

package com.adyen.checkout.core.internal.persistence;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PaymentRepository {
    private static final String PAYMENT_DATABASE = "payment-database.db";

    private static PaymentRepository sInstance;

    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    private final PaymentDatabase mPaymentDatabase;

    @NonNull
    public static synchronized PaymentRepository getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new PaymentRepository(context);
        }

        return sInstance;
    }

    private PaymentRepository(@NonNull Context context) {
        Context applicationContext = context.getApplicationContext();
        mPaymentDatabase = Room
                .databaseBuilder(applicationContext, PaymentDatabase.class, PAYMENT_DATABASE)
                .allowMainThreadQueries()
                .build();
    }

    public void insertPaymentSessionEntity(@NonNull final PaymentSessionEntity paymentSessionEntity) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mPaymentDatabase.getPaymentSessionDao().insert(paymentSessionEntity);
            }
        });
    }

    public void updatePaymentSessionEntity(@NonNull final PaymentSessionEntity paymentSessionEntity) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mPaymentDatabase.getPaymentSessionDao().update(paymentSessionEntity);
            }
        });
    }

    @Nullable
    public PaymentSessionEntity findPaymentSessionEntityByUuid(@NonNull final String uuid) {
        return mPaymentDatabase.getPaymentSessionDao().findByUuid(uuid);
    }

    public void insertPaymentInitiationResponseEntity(@NonNull final PaymentInitiationResponseEntity paymentInitiationResponseEntity) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mPaymentDatabase.getPaymentInitiationResponseDao().insert(paymentInitiationResponseEntity);
            }
        });
    }

    public void updatePaymentInitiationResponseEntity(@NonNull final PaymentInitiationResponseEntity paymentInitiationResponseEntity) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mPaymentDatabase.getPaymentInitiationResponseDao().update(paymentInitiationResponseEntity);
            }
        });
    }

    @Nullable
    public PaymentInitiationResponseEntity findLatestPaymentInitiationResponseEntityByPaymentSessionEntityUuid(@NonNull String uuid) {
        return mPaymentDatabase.getPaymentInitiationResponseDao().findLatestByPaymentSessionEntityUuid(uuid);
    }
}
