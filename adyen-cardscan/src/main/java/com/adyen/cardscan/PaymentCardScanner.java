package com.adyen.cardscan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Base class for classes providing functionality to scan {@link PaymentCard PaymentCards}, e.g. credit cards.
 */
public abstract class PaymentCardScanner {
    private Activity activity;

    private Listener listener;

    /**
     * Construct a new {@link PaymentCardScanner}.
     *
     * @param activity The host {@link Activity}.
     */
    public PaymentCardScanner(@NonNull Activity activity) {
        this.activity = activity;
    }

    /**
     * Forward the result from {@link Activity#onActivityResult(int, int, Intent)} to this method in your integration to allow subclasses of
     * {@link PaymentCardScanner} to handle these results.
     *
     * @param requestCode The request code.
     * @param resultCode The result code
     * @param data The data.
     */
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Intentionally left blank.
    }

    /**
     * Forward to this in the current {@link android.content.Context}'s onResume() lifecycle method to handle it.
     */
    public void onResume() {
        // Intentionally left blank.
    }

    /**
     * Forward to this in the current {@link android.content.Context}'s onResume() lifecycle method to handle it.
     */
    public void onPause() {
        // Intentionally left blank.
    }

    /**
     * Start the scan process.
     *
     * @see #notifyScanStarted()
     * @see #notifyScanCompleted(PaymentCard)
     * @see #notifyScanError(Throwable)
     */
    public abstract void startScan();

    @NonNull
    public abstract Drawable getDisplayIcon();

    /**
     * @return A brief human readable description of how the {@link PaymentCard} will be scanned (e.g. "Scan with camera").
     */
    @NonNull
    public abstract String getDisplayDescription();

    /**
     * @return The host {@link Activity}.
     */
    @NonNull
    public Activity getActivity() {
        return activity;
    }

    /**
     * Set the {@link Listener} of this {@link PaymentCardScanner}.
     *
     * @param listener The new {@link Listener}.
     */
    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    /**
     * Start an {@link Activity} for result.
     *
     * @param intent The {@link Intent} to start.
     * @param requestCode The request code.
     */
    protected void startActivityForResult(@NonNull Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    /**
     * Start an {@link Activity} for result.
     *
     * @param intent The {@link Intent} to start.
     * @param requestCode The request code.
     * @param options The options {@link Bundle}.
     */
    protected void startActivityForResult(@NonNull Intent intent, int requestCode, @Nullable Bundle options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.startActivityForResult(intent, requestCode, options);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Dispatches {@link Listener#onScanStarted(PaymentCardScanner)} on the main thread.
     */
    protected void notifyScanStarted() {
        if (listener != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onScanStarted(PaymentCardScanner.this);
                }
            });
        }
    }

    /**
     * Dispatches {@link Listener#onScanCompleted(PaymentCardScanner, PaymentCard)} on the main thread.
     */
    protected void notifyScanCompleted(@NonNull final PaymentCard paymentCard) {
        if (listener != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onScanCompleted(PaymentCardScanner.this, paymentCard);
                }
            });
        }
    }

    /**
     * Dispatches {@link Listener#onScanError(PaymentCardScanner, Throwable)} on the main thread.
     */
    protected void notifyScanError(@NonNull final Throwable error) {
        if (listener != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onScanError(PaymentCardScanner.this, error);
                }
            });
        }
    }

    /**
     * Listener interface for a {@link PaymentCardScanner}.
     */
    public interface Listener {
        /**
         * Called when the scanning started.
         *
         * @param paymentCardScanner The {@link PaymentCardScanner}.
         */
        void onScanStarted(@NonNull PaymentCardScanner paymentCardScanner);

        /**
         * Called when the scan completed successfully.
         *
         * @param paymentCardScanner The {@link PaymentCardScanner}.
         * @param paymentCard The result {@link PaymentCard}.
         */
        void onScanCompleted(@NonNull PaymentCardScanner paymentCardScanner, @NonNull PaymentCard paymentCard);

        /**
         * Called when an error was encountered during the scan.
         *
         * @param paymentCardScanner The {@link PaymentCardScanner}.
         * @param error The {@link Throwable} that was encountered.
         */
        void onScanError(@NonNull PaymentCardScanner paymentCardScanner, @Nullable Throwable error);
    }
}
