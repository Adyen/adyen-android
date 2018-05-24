package com.adyen.core.internals;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.core.interfaces.PaymentMethodAvailabilityCallback;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.PaymentModule;
import com.adyen.core.services.PaymentMethodService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 *
 * Utility class for retrieving the service for each module.
 */
public final class ModuleAvailabilityUtil {

    @Nullable
    public static PaymentMethodService getModulePaymentService(@NonNull PaymentModule paymentModule)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        PaymentMethodServiceFactory paymentMethodServiceFactory = new PaymentMethodServiceFactory();
        PaymentMethodService paymentMethodService;
        try {
            paymentMethodService = paymentMethodServiceFactory.getService(paymentModule);
        } catch (@NonNull ClassNotFoundException classNotFoundException) {
            //classNotFoundException.printStackTrace();
            throw classNotFoundException;
        }  catch (@NonNull IllegalAccessException illegalAccessException) {
            //illegalAccessException.printStackTrace();
            throw illegalAccessException;
        }  catch (@NonNull InstantiationException instantiationException) {
            //instantiationException.printStackTrace();
            throw instantiationException;
        }

        return paymentMethodService;
    }

    @NonNull
    public static Observable<List<PaymentMethod>> filterPaymentMethods(
            @NonNull final Context context, @NonNull List<PaymentMethod> unfilteredPaymentMethods) {

        return Observable.fromIterable(unfilteredPaymentMethods).concatMap(
                new Function<PaymentMethod, Observable<PaymentMethod>>() {
            @Override
            public Observable<PaymentMethod> apply(final PaymentMethod paymentMethod) {
                return Observable.create(new ObservableOnSubscribe<PaymentMethod>() {
                    @Override
                    public void subscribe(final ObservableEmitter<PaymentMethod> subscriber) {
                        isPaymentMethodAvailable(context, paymentMethod,
                                new PaymentMethodAvailabilityCallback() {
                            @Override
                            public void onSuccess(boolean isAvailable) {
                                if (!subscriber.isDisposed() && isAvailable) {
                                    subscriber.onNext(paymentMethod);
                                }
                                subscriber.onComplete();
                            }

                            @Override
                            public void onFail(Throwable e) {
                                subscriber.onComplete();
                            }
                        });
                    }
                });
            }
        })
        .toList()
        .toObservable()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Check if this payment method is available in the current configuration.
     * @param context The application context. Needed to check if an additional module is installed in case it is
     *                required.
     * @param paymentMethod The payment method to be checked.
     * @param callback The callback that is called after checking if this payment method is available.
     *                 This is always successfull for payment methods that do not require an additional module.
     */
    private static void isPaymentMethodAvailable(final Context context, final PaymentMethod paymentMethod,
                                                 final PaymentMethodAvailabilityCallback callback) {
        if (paymentMethod.getPaymentModule() != null) {
            try {
                PaymentMethodService paymentMethodService = ModuleAvailabilityUtil
                        .getModulePaymentService(PaymentModule.valueOf(paymentMethod.getType()));
                paymentMethodService.checkAvailability(context, paymentMethod, callback);
            } catch (@NonNull Exception exception) {
                callback.onSuccess(false);
            }
        } else {
            callback.onSuccess(true);
        }
    }

    private ModuleAvailabilityUtil() {
        // Private constructor
    }

}
