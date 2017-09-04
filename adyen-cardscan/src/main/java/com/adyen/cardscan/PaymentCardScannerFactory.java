package com.adyen.cardscan;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Factory interface to create all available {@link PaymentCardScanner PaymentCardScanners}.
 */
public interface PaymentCardScannerFactory {
    String MANIFEST_KEY = "CheckoutPaymentCardScannerFactory";

    /**
     * Get all available {@link PaymentCardScanner PaymentCardScanners} for the given {@link Activity}.
     *
     * @param activity The host {@link Activity}.
     * @return A {@link List} of {@link PaymentCardScanner PaymentCardScanners} that is available.
     */
    @NonNull
    List<PaymentCardScanner> getPaymentCardScanners(@NonNull Activity activity);

    /**
     * Utility class to load the {@link PaymentCardScannerFactory}.
     */
    final class Loader {
        private static PaymentCardScannerFactory instance;

        @Nullable
        public static PaymentCardScannerFactory getPaymentCardScannerFactory(@NonNull Context context) {
            if (instance == null) {
                instance = instantiate(context);
            }

            return instance;
        }

        @Nullable
        private static PaymentCardScannerFactory instantiate(@NonNull Context context) {
            try {
                String packageName = context.getPackageName();
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                String factoryClass = applicationInfo.metaData.getString(MANIFEST_KEY);

                if (factoryClass != null) {
                    return (PaymentCardScannerFactory) Class.forName(factoryClass).newInstance();
                } else {
                    return null;
                }
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private Loader() {
            // Hide utility class constructor.
        }
    }
}
