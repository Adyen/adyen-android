/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 08/05/2018.
 */

package com.adyen.checkout.core.internal;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.NetworkingState;
import com.adyen.checkout.core.Observable;
import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.PaymentResult;
import com.adyen.checkout.core.handler.AdditionalDetailsHandler;
import com.adyen.checkout.core.handler.ErrorHandler;
import com.adyen.checkout.core.handler.RedirectHandler;
import com.adyen.checkout.core.internal.model.AdditionalPaymentMethodDetails;
import com.adyen.checkout.core.internal.model.AppResponseDetails;
import com.adyen.checkout.core.internal.model.PaymentInitiation;
import com.adyen.checkout.core.internal.model.PaymentInitiationResponse;
import com.adyen.checkout.core.internal.model.PaymentMethodDeletionResponse;
import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.internal.model.PaymentSessionImpl;
import com.adyen.checkout.core.internal.persistence.PaymentInitiationResponseEntity;
import com.adyen.checkout.core.internal.persistence.PaymentRepository;
import com.adyen.checkout.core.internal.persistence.PaymentSessionEntity;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentMethodDetails;
import com.adyen.checkout.core.model.PaymentSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PaymentHandlerImpl implements PaymentHandler {
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(3);

    private final Application mApplication;

    private final PaymentRepository mPaymentRepository;

    private final ObservableImpl<NetworkingState> mNetworkingStateObservable;

    private final ObservableImpl<PaymentSession> mPaymentSessionObservable;

    private final ObservableImpl<PaymentResult> mPaymentResultObservable;

    private final RedirectManager mRedirectManager;

    private final AdditionalDetailsManager mAdditionalDetailsManager;

    private final ErrorManager mErrorManager;

    private NetworkingStateImpl mNetworkingStateImpl;

    private PaymentSessionEntity mPaymentSessionEntity;

    private PaymentInitiationResponseEntity mPaymentInitiationResponseEntity;

    @NonNull
    public static PaymentReferenceImpl createPaymentReference(@NonNull Activity activity, @NonNull PaymentSessionImpl paymentSession) {
        String paymentSessionUuid = UUID.randomUUID().toString();

        PaymentSessionEntity paymentSessionEntity = new PaymentSessionEntity();
        paymentSessionEntity.uuid = paymentSessionUuid;
        paymentSessionEntity.paymentSession = paymentSession;
        paymentSessionEntity.generationTime = paymentSession.getGenerationTime();

        PaymentRepository.getInstance(activity).insertPaymentSessionEntity(paymentSessionEntity);
        PaymentReferenceImpl paymentReference = new PaymentReferenceImpl(paymentSessionUuid);

        PaymentHandlerImpl paymentHandler = new PaymentHandlerImpl(activity.getApplication(), paymentSessionEntity, null);
        PaymentHandlerStore.getInstance().storePaymentHandler(paymentReference, paymentHandler);

        return paymentReference;
    }

    @NonNull
    public static PaymentHandler getPaymentHandler(@NonNull Activity activity, @NonNull PaymentReference paymentReference) {
        PaymentHandlerImpl paymentHandler = PaymentHandlerStore.getInstance().getPaymentHandler(paymentReference);

        if (paymentHandler == null) {
            PaymentRepository paymentRepository = PaymentRepository.getInstance(activity);
            String paymentReferenceUuid = paymentReference.getUuid();
            PaymentSessionEntity paymentSessionEntity = paymentRepository.findPaymentSessionEntityByUuid(paymentReferenceUuid);

            if (paymentSessionEntity != null) {
                PaymentInitiationResponseEntity paymentInitiationResponseEntity = paymentRepository
                        .findLatestPaymentInitiationResponseEntityByPaymentSessionEntityUuid(paymentReferenceUuid);
                paymentHandler = new PaymentHandlerImpl(activity.getApplication(), paymentSessionEntity, paymentInitiationResponseEntity);
                PaymentHandlerStore.getInstance().storePaymentHandler(paymentReference, paymentHandler);
            } else {
                // TODO: 12/07/2018 Handle.
                throw new RuntimeException();
            }
        }

        return paymentHandler;
    }

    private PaymentHandlerImpl(
            @NonNull Application application,
            @NonNull PaymentSessionEntity paymentSessionEntity,
            @Nullable PaymentInitiationResponseEntity paymentInitiationResponseEntity
    ) {
        mApplication = application;
        mPaymentRepository = PaymentRepository.getInstance(application);

        mNetworkingStateImpl = new NetworkingStateImpl();
        mPaymentSessionEntity = paymentSessionEntity;
        mPaymentInitiationResponseEntity = paymentInitiationResponseEntity;

        mNetworkingStateObservable = new ObservableImpl<NetworkingState>(mNetworkingStateImpl);
        mPaymentSessionObservable = new ObservableImpl<PaymentSession>(mPaymentSessionEntity.paymentSession);
        mPaymentResultObservable = new ObservableImpl<>(null);

        BaseManager.Listener listener = new BaseManager.Listener() {
            @Override
            public void onHandled() {
                mPaymentInitiationResponseEntity.handled = true;
                mPaymentRepository.updatePaymentInitiationResponseEntity(mPaymentInitiationResponseEntity);
            }
        };

        mRedirectManager = new RedirectManager(listener);
        mAdditionalDetailsManager = new AdditionalDetailsManager(listener);
        mErrorManager = new ErrorManager(new BaseManager.Listener() {
            @Override
            public void onHandled() { }
        });

        if (mPaymentInitiationResponseEntity != null) {
            handlePaymentInitiationResponseEntity(mPaymentInitiationResponseEntity);
        }
    }

    @NonNull
    @Override
    public LogoApi getLogoApi() {
        return LogoApi.getInstance(mApplication, mPaymentSessionEntity.paymentSession.getLogoApiHostProvider());
    }

    @NonNull
    @Override
    public Observable<NetworkingState> getNetworkingStateObservable() {
        return mNetworkingStateObservable;
    }

    @NonNull
    @Override
    public Observable<PaymentSession> getPaymentSessionObservable() {
        return mPaymentSessionObservable;
    }

    @NonNull
    @Override
    public Observable<PaymentResult> getPaymentResultObservable() {
        return mPaymentResultObservable;
    }

    @Override
    public void setRedirectHandler(@NonNull Activity activity, @NonNull RedirectHandler redirectHandler) {
        mRedirectManager.addHandler(activity, redirectHandler);
    }

    @Override
    public void setAdditionalDetailsHandler(@NonNull Activity activity, @NonNull AdditionalDetailsHandler additionalDetailsHandler) {
        mAdditionalDetailsManager.addHandler(activity, additionalDetailsHandler);
    }

    @Override
    public void setErrorHandler(@NonNull Activity activity, @NonNull ErrorHandler errorHandler) {
        mErrorManager.addHandler(activity, errorHandler);
    }

    @Override
    public void initiatePayment(@NonNull PaymentMethod paymentMethod, @Nullable PaymentMethodDetails paymentMethodDetails) {
        PaymentSessionImpl paymentSession = mPaymentSessionEntity.paymentSession;
        initiatePaymentInternal(paymentSession, (PaymentMethodImpl) paymentMethod, paymentMethodDetails);
    }

    @Override
    public void submitAdditionalDetails(@NonNull PaymentMethodDetails paymentMethodDetails) {
        PaymentSessionImpl paymentSession = mPaymentSessionEntity.paymentSession;
        PaymentInitiationResponse.DetailFields detailFields = mPaymentInitiationResponseEntity.paymentInitiationResponse.getDetailFields();

        if (detailFields == null) {
            CheckoutException checkoutException = new CheckoutException.Builder(
                    "Could not submit additional details, DetailFields == null.",
                    null
            ).build();
            handleCheckoutException(checkoutException);
            return;
        }

        PaymentMethodImpl paymentMethod = mPaymentInitiationResponseEntity.paymentMethod;

        if (paymentMethodDetails instanceof AdditionalPaymentMethodDetails) {
            try {
                AdditionalPaymentMethodDetails.finalize(((AdditionalPaymentMethodDetails) paymentMethodDetails), detailFields);
            } catch (CheckoutException e) {
                handleCheckoutException(e);
                return;
            }
        }

        initiatePaymentInternal(paymentSession, paymentMethod, paymentMethodDetails);
    }

    @Override
    public void handleRedirectResult(@NonNull final Uri redirectResult) {
        PaymentSessionImpl paymentSession = mPaymentSessionEntity.paymentSession;
        PaymentInitiationResponse.RedirectFields redirectFields = mPaymentInitiationResponseEntity.paymentInitiationResponse.getRedirectFields();

        if (redirectFields == null) {
            CheckoutException checkoutException = new CheckoutException.Builder(
                    "Could not handle redirect result, RedirectFields == null.",
                    null
            ).build();
            handleCheckoutException(checkoutException);
            return;
        }

        PaymentMethodImpl paymentMethod = mPaymentInitiationResponseEntity.paymentMethod;

        if (redirectFields.isSubmitPaymentMethodReturnData()) {
            AppResponseDetails appResponseDetails = new AppResponseDetails.Builder(redirectResult.getQuery()).build();
            initiatePaymentInternal(paymentSession, paymentMethod, appResponseDetails);
        } else {
            try {
                JSONObject jsonObject = new JSONObject();

                for (String name : redirectResult.getQueryParameterNames()) {
                    jsonObject.put(name, redirectResult.getQueryParameter(name));
                }

                PaymentInitiationResponse paymentInitiationResponse = JsonObject.parseFrom(jsonObject, PaymentInitiationResponse.class);
                handlePaymentInitiationResponse(paymentMethod, paymentInitiationResponse);
            } catch (JSONException e) {
                CheckoutException checkoutException = new CheckoutException.Builder("Could not parse PaymentInitiationResponse.", e).build();
                handleCheckoutException(checkoutException);
            }
        }
    }

    @Override
    public void deleteOneClickPaymentMethod(@NonNull final PaymentMethod paymentMethod) {
        PaymentSessionImpl paymentSession = mPaymentSessionEntity.paymentSession;

        List<PaymentMethodImpl> oneClickPaymentMethods = paymentSession.getOneClickPaymentMethodImpls();
        //noinspection SuspiciousMethodCalls
        int index = oneClickPaymentMethods != null ? oneClickPaymentMethods.indexOf(paymentMethod) : -1;

        if (index >= 0) {
            PaymentMethodImpl paymentMethodImpl = oneClickPaymentMethods.get(index);
            deletePaymentMethod(paymentSession, paymentMethodImpl);
        } else {
            CheckoutException checkoutException = new CheckoutException
                    .Builder("Cannot delete payment method that is not a one click payment method.", null).build();
            handleCheckoutException(checkoutException);
        }
    }

    private void deletePaymentMethod(@NonNull final PaymentSessionImpl paymentSession, @NonNull final PaymentMethodImpl paymentMethod) {
        final Callable<PaymentMethodDeletionResponse> callable = CheckoutApi
                .getInstance(mApplication)
                .deletePaymentMethod(paymentSession, paymentMethod);

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                handleRequestStarted();

                try {
                    PaymentMethodDeletionResponse response = callable.call();

                    if (response.getResultCode() == PaymentMethodDeletionResponse.ResultCode.SUCCESS) {
                        PaymentSessionImpl updatedPaymentSession = paymentSession.copyByRemovingOneClickPaymentMethod(paymentMethod);
                        handleUpdatedPaymentSession(updatedPaymentSession);
                    } else {
                        CheckoutException checkoutException = new CheckoutException.Builder("Could not delete PaymentMethod.", null).build();
                        handleCheckoutException(checkoutException);
                    }
                } catch (Exception e) {
                    handleException(e, "An error occurred while deleting the payment method.");
                } finally {
                    handleRequestFinished();
                }
            }
        });
    }

    private void initiatePaymentInternal(
            @NonNull PaymentSessionImpl paymentSession,
            @NonNull final PaymentMethodImpl paymentMethod,
            @Nullable PaymentMethodDetails paymentMethodDetails
    ) {
        String paymentData = paymentSession.getPaymentData();
        String paymentMethodData = paymentMethod.getPaymentMethodData();
        PaymentInitiation paymentInitiation = new PaymentInitiation.Builder(paymentData, paymentMethodData)
                .setPaymentMethodDetails(paymentMethodDetails)
                .build();

        final Callable<PaymentInitiationResponse> callable = CheckoutApi
                .getInstance(mApplication)
                .initiatePayment(paymentSession, paymentInitiation);

        handleRequestStarted();
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    PaymentInitiationResponse response = callable.call();
                    handlePaymentInitiationResponse(paymentMethod, response);
                } catch (Exception e) {
                    handleException(e, "An error occurred while initiating the payment.");
                } finally {
                    handleRequestFinished();
                }
            }
        });
    }

    private void handleRequestStarted() {
        mNetworkingStateImpl.onRequestStarted();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNetworkingStateObservable.setValue(mNetworkingStateImpl);
            }
        });
    }

    private void handleRequestFinished() {
        mNetworkingStateImpl.onRequestFinished();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNetworkingStateObservable.setValue(mNetworkingStateImpl);
            }
        });
    }

    private void handleUpdatedPaymentSession(@NonNull final PaymentSessionImpl updatedPaymentSession) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPaymentSessionObservable.setValue(updatedPaymentSession);
            }
        });

        PaymentSessionEntity paymentSessionEntity = new PaymentSessionEntity();
        paymentSessionEntity.uuid = mPaymentSessionEntity.uuid;
        paymentSessionEntity.paymentSession = updatedPaymentSession;
        paymentSessionEntity.generationTime = updatedPaymentSession.getGenerationTime();

        mPaymentSessionEntity = paymentSessionEntity;
        mPaymentRepository.updatePaymentSessionEntity(paymentSessionEntity);
    }

    private void handlePaymentInitiationResponse(
            @NonNull PaymentMethodImpl paymentMethod,
            @NonNull PaymentInitiationResponse paymentInitiationResponse
    ) {
        PaymentInitiationResponse.ErrorFields errorFields = paymentInitiationResponse.getErrorFields();

        if (errorFields != null) {
            CheckoutException checkoutException = new CheckoutException.Builder(errorFields.getErrorMessage(), null)
                    .setPayload(errorFields.getPayload())
                    .setFatal(errorFields.getErrorCode() == PaymentInitiationResponse.ErrorCode.PAYMENT_SESSION_EXPIRED)
                    .build();
            handleCheckoutException(checkoutException);
            return;
        }

        PaymentInitiationResponseEntity paymentInitiationResponseEntity = new PaymentInitiationResponseEntity();
        paymentInitiationResponseEntity.paymentSessionUuid = mPaymentSessionEntity.uuid;
        paymentInitiationResponseEntity.paymentMethod = paymentMethod;
        paymentInitiationResponseEntity.paymentInitiationResponse = paymentInitiationResponse;

        handlePaymentInitiationResponseEntity(paymentInitiationResponseEntity);

        mPaymentInitiationResponseEntity = paymentInitiationResponseEntity;
        mPaymentRepository.insertPaymentInitiationResponseEntity(paymentInitiationResponseEntity);
    }

    private void handlePaymentInitiationResponseEntity(@NonNull final PaymentInitiationResponseEntity paymentInitiationResponseEntity) {
        runOnUiThread(new Runnable() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void run() {
                PaymentInitiationResponse paymentInitiationResponse = paymentInitiationResponseEntity.paymentInitiationResponse;

                switch (paymentInitiationResponse.getType()) {
                    case REDIRECT:
                        if (!paymentInitiationResponseEntity.handled) {
                            mRedirectManager.setData(paymentInitiationResponse.getRedirectFields());
                        }
                        break;
                    case DETAILS:
                        if (!paymentInitiationResponseEntity.handled) {
                            mAdditionalDetailsManager.setData(paymentInitiationResponse.getDetailFields());
                        }
                        break;
                    case COMPLETE:
                        mPaymentResultObservable.setValue(paymentInitiationResponse.getCompleteFields());
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void handleException(@NonNull Exception exception, @NonNull String fallbackMessage) {
        CheckoutException checkoutException = exception instanceof CheckoutException
                ? (CheckoutException) exception
                : new CheckoutException.Builder(fallbackMessage, exception).build();
        handleCheckoutException(checkoutException);
    }

    private void handleCheckoutException(@NonNull final CheckoutException checkoutException) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mErrorManager.setData(checkoutException);
            }
        });
    }

    private void runOnUiThread(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            MAIN_HANDLER.post(runnable);
        }
    }
}
