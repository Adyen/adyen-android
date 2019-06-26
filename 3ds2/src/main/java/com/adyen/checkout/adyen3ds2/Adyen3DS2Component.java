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
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;

import com.adyen.checkout.adyen3ds2.model.ChallengeResult;
import com.adyen.checkout.adyen3ds2.model.ChallengeToken;
import com.adyen.checkout.adyen3ds2.model.FingerprintToken;
import com.adyen.checkout.base.ActionComponentData;
import com.adyen.checkout.base.DetailsComponentProvider;
import com.adyen.checkout.base.ComponentError;
import com.adyen.checkout.base.component.BaseActionComponent;
import com.adyen.checkout.base.component.DetailsComponentProviderImpl;
import com.adyen.checkout.base.encoding.Base64Encoder;
import com.adyen.checkout.base.model.payments.response.Action;
import com.adyen.checkout.base.model.payments.response.Threeds2ChallengeAction;
import com.adyen.checkout.base.model.payments.response.Threeds2FingerprintAction;
import com.adyen.checkout.core.exeption.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.core.util.StringUtil;
import com.adyen.threeds2.AuthenticationRequestParameters;
import com.adyen.threeds2.ChallengeStatusReceiver;
import com.adyen.threeds2.CompletionEvent;
import com.adyen.threeds2.ProtocolErrorEvent;
import com.adyen.threeds2.RuntimeErrorEvent;
import com.adyen.threeds2.ThreeDS2Service;
import com.adyen.threeds2.Transaction;
import com.adyen.threeds2.exception.SDKAlreadyInitializedException;
import com.adyen.threeds2.exception.SDKNotInitializedException;
import com.adyen.threeds2.parameters.ChallengeParameters;
import com.adyen.threeds2.parameters.ConfigParameters;
import com.adyen.threeds2.util.AdyenConfigParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Adyen3DS2Component extends BaseActionComponent implements ChallengeStatusReceiver {
    private static final String TAG = LogUtil.getTag();

    public static final DetailsComponentProvider<Adyen3DS2Component> PROVIDER = new DetailsComponentProviderImpl<>(Adyen3DS2Component.class);

    private static final String FINGERPRINT_DETAILS_KEY = "threeds2.fingerprint";
    private static final String CHALLENGE_DETAILS_KEY = "threeds2.challengeResult";

    private static final int DEFAULT_CHALLENGE_TIME_OUT = 10;
    private static boolean sGotDestroyedWhileChallenging = false;

    private Transaction mTransaction;

    public Adyen3DS2Component(@NonNull Application application) {
        super(application);
    }

    //TODO review error flows

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

    @Override
    @NonNull
    protected List<String> getSupportedActionTypes() {
        final String[] supportedCodes = {Threeds2FingerprintAction.ACTION_TYPE, Threeds2ChallengeAction.ACTION_TYPE};
        return Collections.unmodifiableList(Arrays.asList(supportedCodes));
    }

    @Override
    public void handleActionInternal(@NonNull Activity activity, @NonNull Action action) {
        if (Threeds2FingerprintAction.ACTION_TYPE.equals(action.getType())) {
            final Threeds2FingerprintAction fingerprintAction = (Threeds2FingerprintAction) action;
            if (!StringUtil.hasContent(fingerprintAction.getToken())) {
                notifyError(new ComponentError("Fingerprint token not found."));
                return;
            }
            final String encodedFingerprint = identifyShopper(activity, fingerprintAction.getToken());
            try {
                notifyDetails(createFingerprintDetails(encodedFingerprint));
            } catch (JSONException e) {
                Logger.e(TAG, "Failed to create fingerprint.", e);
                notifyError(new ComponentError("Failed to create fingerprint."));
            }
        } else if (Threeds2ChallengeAction.ACTION_TYPE.equals(action.getType())) {
            final Threeds2ChallengeAction challengeAction = (Threeds2ChallengeAction) action;
            if (!StringUtil.hasContent(challengeAction.getToken())) {
                notifyError(new ComponentError("Challenge token not found."));
                return;
            }
            challengeShopper(activity, challengeAction.getToken());
        }
    }

    @Override
    public void completed(@NonNull CompletionEvent completionEvent) {
        Logger.d(TAG, "challenge completed");
        try {
            final ChallengeResult result = ChallengeResult.from(completionEvent);
            notifyDetails(createChallengeDetails(result));
            closeTransaction(getApplication());
        } catch (JSONException e) {
            throw new CheckoutException("Failed to parse challenge result.", e);
        }
    }

    @Override
    public void cancelled() {
        Logger.d(TAG, "challenge cancelled");
        closeTransaction(getApplication());
    }

    @Override
    public void timedout() {
        Logger.d(TAG, "challenge timedout");
        closeTransaction(getApplication());
    }

    @Override
    public void protocolError(@NonNull ProtocolErrorEvent protocolErrorEvent) {
        Logger.d(TAG, "protocolError");
        closeTransaction(getApplication());
    }

    @Override
    public void runtimeError(@NonNull RuntimeErrorEvent runtimeErrorEvent) {
        Logger.d(TAG, "runtimeError");
        closeTransaction(getApplication());
    }

    private String identifyShopper(Context context, String encodedFingerprintToken) {
        Logger.d(TAG, "identifyShopper");
        final String decodedFingerprintToken = Base64Encoder.decode(encodedFingerprintToken);

        final JSONObject fingerprintJson;
        try {
            fingerprintJson = new JSONObject(decodedFingerprintToken);
        } catch (JSONException e) {
            throw new CheckoutException("JSON parsing of FingerprintToken failed", e);
        }

        final FingerprintToken fingerprintToken = FingerprintToken.SERIALIZER.deserialize(fingerprintJson);
        final ConfigParameters configParameters = new AdyenConfigParameters.Builder(fingerprintToken.getDirectoryServerId(),
                fingerprintToken.getDirectoryServerPublicKey()).build();

        try {
            ThreeDS2Service.INSTANCE.initialize(context, configParameters, null, null);
        } catch (SDKAlreadyInitializedException e) {
            // This shouldn't cause any side effect.
            Logger.e(TAG, "3DS2 Service already initialized.");
        }

        try {
            mTransaction = ThreeDS2Service.INSTANCE.createTransaction(null, null);
        } catch (SDKNotInitializedException e) {
            throw new CheckoutException("Failed to create transaction", e);
        }

        final AuthenticationRequestParameters authenticationRequestParameters = mTransaction.getAuthenticationRequestParameters();

        return createEncodedFingerprint(authenticationRequestParameters);
    }

    private void challengeShopper(Activity activity, String encodedChallengeToken) {
        Logger.d(TAG, "challengeShopper");

        final String decodedChallengeToken = Base64Encoder.decode(encodedChallengeToken);

        final JSONObject challengeTokenJson;
        try {
            challengeTokenJson = new JSONObject(decodedChallengeToken);
        } catch (JSONException e) {
            throw new CheckoutException("JSON parsing of FingerprintToken failed", e);
        }

        final ChallengeToken challengeToken = ChallengeToken.SERIALIZER.deserialize(challengeTokenJson);
        final ChallengeParameters challengeParameters = createChallengeParameters(challengeToken);

        mTransaction.doChallenge(activity, challengeParameters, this, DEFAULT_CHALLENGE_TIME_OUT);
    }

    @NonNull
    private String createEncodedFingerprint(AuthenticationRequestParameters authenticationRequestParameters) {

        final JSONObject fingerprintJson = new JSONObject();
        try {
            // TODO getMessageVersion is not used?
            fingerprintJson.put("sdkAppID", authenticationRequestParameters.getSDKAppID());
            fingerprintJson.put("sdkEncData", authenticationRequestParameters.getDeviceData());
            fingerprintJson.put("sdkEphemPubKey", new JSONObject(authenticationRequestParameters.getSDKEphemeralPublicKey()));
            fingerprintJson.put("sdkReferenceNumber", authenticationRequestParameters.getSDKReferenceNumber());
            fingerprintJson.put("sdkTransID", authenticationRequestParameters.getSDKTransactionID());
        } catch (JSONException e) {
            throw new CheckoutException("Failed to create fingerprintJson", e);
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

    private JSONObject createFingerprintDetails(String encodedFingerprint) throws JSONException {
        final JSONObject fingerprintDetails = new JSONObject();
        fingerprintDetails.put(FINGERPRINT_DETAILS_KEY, encodedFingerprint);
        return fingerprintDetails;
    }

    private JSONObject createChallengeDetails(ChallengeResult challengeResult) throws JSONException {
        final JSONObject challengeDetails = new JSONObject();
        challengeDetails.put(CHALLENGE_DETAILS_KEY, challengeResult.getPayload());
        return challengeDetails;
    }
}
