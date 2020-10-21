/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/5/2019.
 */

package com.adyen.checkout.adyen3ds2;

import android.app.Activity;
import android.app.Application;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.adyen3ds2.exception.Authentication3DS2Exception;
import com.adyen.checkout.adyen3ds2.exception.Cancelled3DS2Exception;
import com.adyen.checkout.adyen3ds2.model.ChallengeResult;
import com.adyen.checkout.adyen3ds2.model.ChallengeToken;
import com.adyen.checkout.adyen3ds2.model.FingerprintToken;
import com.adyen.checkout.base.ActionComponentData;
import com.adyen.checkout.base.ActionComponentProvider;
import com.adyen.checkout.base.component.ActionComponentProviderImpl;
import com.adyen.checkout.base.component.BaseActionComponent;
import com.adyen.checkout.base.encoding.Base64Encoder;
import com.adyen.checkout.base.model.payments.response.Action;
import com.adyen.checkout.base.model.payments.response.Threeds2ChallengeAction;
import com.adyen.checkout.base.model.payments.response.Threeds2FingerprintAction;
import com.adyen.checkout.core.api.ThreadManager;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.exception.ComponentException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.threeds2.AuthenticationRequestParameters;
import com.adyen.threeds2.ChallengeStatusReceiver;
import com.adyen.threeds2.CompletionEvent;
import com.adyen.threeds2.ProtocolErrorEvent;
import com.adyen.threeds2.RuntimeErrorEvent;
import com.adyen.threeds2.ThreeDS2Service;
import com.adyen.threeds2.Transaction;
import com.adyen.threeds2.customization.UiCustomization;
import com.adyen.threeds2.exception.InvalidInputException;
import com.adyen.threeds2.exception.SDKAlreadyInitializedException;
import com.adyen.threeds2.exception.SDKNotInitializedException;
import com.adyen.threeds2.exception.SDKRuntimeException;
import com.adyen.threeds2.parameters.ChallengeParameters;
import com.adyen.threeds2.parameters.ConfigParameters;
import com.adyen.threeds2.util.AdyenConfigParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Adyen3DS2Component extends BaseActionComponent<Adyen3DS2Configuration> implements ChallengeStatusReceiver {
    @SuppressWarnings(Lint.SYNTHETIC)
    static final String TAG = LogUtil.getTag();

    public static final ActionComponentProvider<Adyen3DS2Component> PROVIDER =
            new ActionComponentProviderImpl<>(Adyen3DS2Component.class, Adyen3DS2Configuration.class);

    private static final String FINGERPRINT_DETAILS_KEY = "threeds2.fingerprint";
    private static final String CHALLENGE_DETAILS_KEY = "threeds2.challengeResult";

    private static final int DEFAULT_CHALLENGE_TIME_OUT = 10;
    private static boolean sGotDestroyedWhileChallenging = false;

    @SuppressWarnings(Lint.SYNTHETIC)
    Transaction mTransaction;

    @SuppressWarnings(Lint.SYNTHETIC)
    UiCustomization mUiCustomization;

    public Adyen3DS2Component(@NonNull Application application, @Nullable Adyen3DS2Configuration configuration) {
        super(application, configuration);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Logger.d(TAG, "onCleared");
        if (mTransaction != null) {
            // We don't save the mTransaction reference if the ViewModel gets cleared.
            sGotDestroyedWhileChallenging = true;
        }
    }

    @Override
    public void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<ActionComponentData> observer) {
        super.observe(lifecycleOwner, observer);
        if (sGotDestroyedWhileChallenging) {
            // This is OK if the user goes back and starts a new payment.
            // TODO notify error to activity?
            Logger.e(TAG, "Lost challenge result reference.");
        }
    }

    /**
     * Set a {@link UiCustomization} object to be passed to the 3DS2 SDK for customizing the challenge screen.
     * Needs to be set before handling any action.
     *
     * @param uiCustomization The customization object.
     */
    public void setUiCustomization(@Nullable UiCustomization uiCustomization) {
        synchronized (this) {
            mUiCustomization = uiCustomization;
        }
    }

    @Override
    @NonNull
    protected List<String> getSupportedActionTypes() {
        final String[] supportedCodes = {Threeds2FingerprintAction.ACTION_TYPE, Threeds2ChallengeAction.ACTION_TYPE};
        return Collections.unmodifiableList(Arrays.asList(supportedCodes));
    }

    @Override
    protected void handleActionInternal(@NonNull Activity activity, @NonNull Action action) throws ComponentException {
        if (Threeds2FingerprintAction.ACTION_TYPE.equals(action.getType())) {
            final Threeds2FingerprintAction fingerprintAction = (Threeds2FingerprintAction) action;
            if (TextUtils.isEmpty(fingerprintAction.getToken())) {
                throw new ComponentException("Fingerprint token not found.");
            }

            identifyShopper(activity, fingerprintAction.getToken());

        } else if (Threeds2ChallengeAction.ACTION_TYPE.equals(action.getType())) {
            final Threeds2ChallengeAction challengeAction = (Threeds2ChallengeAction) action;
            if (TextUtils.isEmpty(challengeAction.getToken())) {
                throw new ComponentException("Challenge token not found.");
            }
            challengeShopper(activity, challengeAction.getToken());
        }
    }

    @Override
    public void completed(@NonNull CompletionEvent completionEvent) {
        Logger.d(TAG, "challenge completed");
        try {
            notifyDetails(createChallengeDetails(completionEvent));
        } catch (CheckoutException e) {
            notifyException(e);
        } finally {
            closeTransaction(getApplication());
        }
    }

    @Override
    public void cancelled() {
        Logger.d(TAG, "challenge cancelled");
        notifyException(new Cancelled3DS2Exception("Challenge canceled."));
        closeTransaction(getApplication());
    }

    @Override
    public void timedout() {
        Logger.d(TAG, "challenge timed out");
        notifyException(new Authentication3DS2Exception("Challenge timed out."));
        closeTransaction(getApplication());
    }

    @Override
    public void protocolError(@NonNull ProtocolErrorEvent protocolErrorEvent) {
        Logger.d(TAG, "protocolError");
        notifyException(new Authentication3DS2Exception("Protocol Error - " + protocolErrorEvent.getErrorMessage()));
        closeTransaction(getApplication());
    }

    @Override
    public void runtimeError(@NonNull RuntimeErrorEvent runtimeErrorEvent) {
        Logger.d(TAG, "runtimeError");
        notifyException(new Authentication3DS2Exception("Runtime Error - " + runtimeErrorEvent.getErrorMessage()));
        closeTransaction(getApplication());
    }

    private void identifyShopper(final Context context, final String encodedFingerprintToken) throws ComponentException {
        Logger.d(TAG, "identifyShopper");
        final String decodedFingerprintToken = Base64Encoder.decode(encodedFingerprintToken);

        final JSONObject fingerprintJson;
        try {
            fingerprintJson = new JSONObject(decodedFingerprintToken);
        } catch (JSONException e) {
            throw new ComponentException("JSON parsing of FingerprintToken failed", e);
        }

        final FingerprintToken fingerprintToken = FingerprintToken.SERIALIZER.deserialize(fingerprintJson);
        final ConfigParameters configParameters = new AdyenConfigParameters.Builder(fingerprintToken.getDirectoryServerId(),
                fingerprintToken.getDirectoryServerPublicKey()).build();


        ThreadManager.EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.d(TAG, "initialize 3DS2 SDK");
                    synchronized (Adyen3DS2Component.this) {
                        ThreeDS2Service.INSTANCE.initialize(context, configParameters, null, mUiCustomization);
                    }
                } catch (final SDKRuntimeException e) {
                    notifyException(new ComponentException("Failed to initialize 3DS2 SDK", e));
                    return;
                } catch (SDKAlreadyInitializedException e) {
                    // This shouldn't cause any side effect.
                    Logger.w(TAG, "3DS2 Service already initialized.");
                }

                try {
                    Logger.d(TAG, "create transaction");
                    mTransaction = ThreeDS2Service.INSTANCE.createTransaction(null, null);
                } catch (final SDKNotInitializedException | SDKRuntimeException e) {
                    notifyException(new ComponentException("Failed to create 3DS2 Transaction", e));
                    return;
                }

                final AuthenticationRequestParameters authenticationRequestParameters = mTransaction.getAuthenticationRequestParameters();

                final String encodedFingerprint = createEncodedFingerprint(authenticationRequestParameters);

                ThreadManager.MAIN_HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDetails(createFingerprintDetails(encodedFingerprint));
                        }
                    }
                );
            }
        });
    }

    private void challengeShopper(Activity activity, String encodedChallengeToken) throws ComponentException {
        Logger.d(TAG, "challengeShopper");

        if (mTransaction == null) {
            notifyException(new Authentication3DS2Exception("Failed to make challenge, missing reference to initial transaction."));
            return;
        }

        final String decodedChallengeToken = Base64Encoder.decode(encodedChallengeToken);

        final JSONObject challengeTokenJson;
        try {
            challengeTokenJson = new JSONObject(decodedChallengeToken);
        } catch (JSONException e) {
            throw new ComponentException("JSON parsing of FingerprintToken failed", e);
        }

        final ChallengeToken challengeToken = ChallengeToken.SERIALIZER.deserialize(challengeTokenJson);
        final ChallengeParameters challengeParameters = createChallengeParameters(challengeToken);

        try {
            mTransaction.doChallenge(activity, challengeParameters, this, DEFAULT_CHALLENGE_TIME_OUT);
        } catch (InvalidInputException e) {
            notifyException(new CheckoutException("Error starting challenge", e));
        }
    }

    @NonNull
    @SuppressWarnings(Lint.SYNTHETIC)
    String createEncodedFingerprint(AuthenticationRequestParameters authenticationRequestParameters) throws ComponentException {

        final JSONObject fingerprintJson = new JSONObject();
        try {
            // TODO getMessageVersion is not used?
            fingerprintJson.put("sdkAppID", authenticationRequestParameters.getSDKAppID());
            fingerprintJson.put("sdkEncData", authenticationRequestParameters.getDeviceData());
            fingerprintJson.put("sdkEphemPubKey", new JSONObject(authenticationRequestParameters.getSDKEphemeralPublicKey()));
            fingerprintJson.put("sdkReferenceNumber", authenticationRequestParameters.getSDKReferenceNumber());
            fingerprintJson.put("sdkTransID", authenticationRequestParameters.getSDKTransactionID());
        } catch (JSONException e) {
            throw new ComponentException("Failed to create encoded fingerprint", e);
        }

        return Base64Encoder.encode(fingerprintJson.toString());
    }

    @NonNull
    private ChallengeParameters createChallengeParameters(@NonNull ChallengeToken challenge) {
        // TODO referenceNumber, URL and version are not used?
        final ChallengeParameters challengeParameters = new ChallengeParameters();
        challengeParameters.set3DSServerTransactionID(challenge.getThreeDSServerTransID());
        challengeParameters.setAcsTransactionID(challenge.getAcsTransID());
        challengeParameters.setAcsRefNumber(challenge.getAcsReferenceNumber());
        challengeParameters.setAcsSignedContent(challenge.getAcsSignedContent());
        return challengeParameters;
    }

    @SuppressWarnings("PMD.NullAssignment")
    private void closeTransaction(Context context) {
        if (mTransaction != null) {
            mTransaction.close();
            mTransaction = null;
            try {
                ThreeDS2Service.INSTANCE.cleanup(context);
            } catch (SDKNotInitializedException e) {
                // no problem
            }
        }
    }

    @SuppressWarnings(Lint.SYNTHETIC)
    JSONObject createFingerprintDetails(String encodedFingerprint) throws ComponentException {
        final JSONObject fingerprintDetails = new JSONObject();
        try {
            fingerprintDetails.put(FINGERPRINT_DETAILS_KEY, encodedFingerprint);
        } catch (JSONException e) {
            throw new ComponentException("Failed to create fingerprint details", e);
        }
        return fingerprintDetails;
    }

    private JSONObject createChallengeDetails(@NonNull CompletionEvent completionEvent) throws ComponentException {
        final JSONObject challengeDetails = new JSONObject();
        try {
            final ChallengeResult challengeResult = ChallengeResult.from(completionEvent);
            challengeDetails.put(CHALLENGE_DETAILS_KEY, challengeResult.getPayload());
        } catch (JSONException e) {
            throw new ComponentException("Failed to create challenge details", e);
        }
        return challengeDetails;
    }
}
