package com.adyen.checkout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.adyen.cardscan.PaymentCard;
import com.adyen.cardscan.PaymentCardScanner;
import com.adyen.cardscan.PaymentCardScannerFactory;
import com.adyen.ui.activities.TranslucentLoadingScreenActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by timon on 31/08/2017.
 */
public class MyPaymentCardScannerFactory implements PaymentCardScannerFactory {
    @NonNull
    @Override
    public List<PaymentCardScanner> getPaymentCardScanners(@NonNull Activity activity) {
        final List<PaymentCardScanner> result = new ArrayList<>();
        result.add(new PaymentCardScanner(activity) {
            @Override
            public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                super.onActivityResult(requestCode, resultCode, data);

                if (requestCode == 10) {
                    PaymentCard paymentCard = new PaymentCard.Builder()
                            .setCardNumber("4111111111111111")
                            .setExpiryMonth(10)
                            .setExpiryYear(20)
                            .setSecurityCode("737")
                            .build();

                    notifyScanCompleted(paymentCard);
                }
            }

            @Override
            public void startScan() {
                notifyScanStarted();
                final WeakReference<Activity> activityRef = new WeakReference<>(getActivity());
                startActivityForResult(new Intent(activityRef.get(), TranslucentLoadingScreenActivity.class), 10);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Activity strongRef = activityRef.get();

                        if (strongRef != null) {
                            strongRef.finishActivity(10);
                        }
                    }
                }, 1000);
            }

            @NonNull
            @Override
            public Drawable getDisplayIcon() {
                return ContextCompat.getDrawable(getActivity(), R.drawable.ic_camera_alt_black_24dp);
            }

            @NonNull
            @Override
            public String getDisplayDescription() {
                return "Scan with Camera";
            }
        });

        return result;
    }
}
