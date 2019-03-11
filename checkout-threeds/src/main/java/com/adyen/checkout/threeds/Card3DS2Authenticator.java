/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 16/11/2018.
 */

package com.adyen.checkout.threeds;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.Base64Coder;
import com.adyen.checkout.threeds.internal.ChallengeResultImpl;
import com.adyen.checkout.threeds.internal.model.Challenge;
import com.adyen.checkout.threeds.internal.model.Fingerprint;
import com.adyen.checkout.threeds.internal.model.FingerprintToken;
import com.adyen.threeds2.AuthenticationRequestParameters;
import com.adyen.threeds2.ChallengeStatusReceiver;
import com.adyen.threeds2.CompletionEvent;
import com.adyen.threeds2.ErrorMessage;
import com.adyen.threeds2.ProtocolErrorEvent;
import com.adyen.threeds2.RuntimeErrorEvent;
import com.adyen.threeds2.ThreeDS2Service;
import com.adyen.threeds2.Transaction;
import com.adyen.threeds2.customization.UiCustomization;
import com.adyen.threeds2.exception.SDKAlreadyInitializedException;
import com.adyen.threeds2.exception.SDKNotInitializedException;
import com.adyen.threeds2.parameters.ChallengeParameters;
import com.adyen.threeds2.parameters.ConfigParameters;
import com.adyen.threeds2.util.AdyenConfigParameters;

import org.json.JSONException;

public final class Card3DS2Authenticator {

    private static final int DEFAULT_CHALLENGE_TIME_OUT = 10;

    private Activity mActivity;

    private ListenerDelegate mListenerDelegate;

    private Transaction mTransaction;

    private UiCustomization mUiCustomization;

    /**
     * Initializes the 3DS2 card authenticator.
     * <p/>
     *
     * @param activity The current activity
     * @param listener {@link AuthenticationListener} the 3DS2 authentication listener
     */
    public Card3DS2Authenticator(@NonNull Activity activity, @NonNull AuthenticationListener listener) {
        this(activity, null, listener);
    }

    /**
     * Initializes the 3DS2 card authenticator.
     * <p/>
     *
     * @param activity        The current activity.
     * @param uiCustomization (optional) The {@link UiCustomization}, UI configuration information that is used to specify the UI layout and theme.
     * @param listener        {@link AuthenticationListener} which will be invoked on authentication success with the challenge result or failure.
     */
    @SuppressWarnings("WeakerAccess")
    public Card3DS2Authenticator(@NonNull Activity activity, @Nullable UiCustomization uiCustomization, @NonNull AuthenticationListener listener) {
        mActivity = activity;
        mUiCustomization = uiCustomization;
        mListenerDelegate = new ListenerDelegate(listener);
    }

    /**
     * Creates a fingerprint using a fingerprint token received from the Checkout API.
     * <p/>
     *
     * @param encodedFingerprintToken The fingerprint token received from the Checkout API.
     * @return The encoded device fingerprint.
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public String createFingerprint(@NonNull String encodedFingerprintToken) throws ThreeDS2Exception {
        FingerprintToken fingerprintToken;
        try {
            fingerprintToken = Base64Coder.decode(encodedFingerprintToken, FingerprintToken.class);
        } catch (JSONException e) {
            throw ThreeDS2Exception.from("Fingerprint token decoding failure.", e);
        }

        return createFingerprint(fingerprintToken.getDirectoryServerId(), fingerprintToken.getDirectoryServerPublicKey());
    }

    /**
     * Creates a fingerprint using a directory server identifier and public key.
     * <p/>
     *
     * @param directoryServerId        The directory server identifier.
     * @param directoryServerPublicKey The directory server public key.
     * @return The encoded device fingerprint.
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public String createFingerprint(@NonNull String directoryServerId, @NonNull String directoryServerPublicKey) throws ThreeDS2Exception {
        ConfigParameters configParameters = AdyenConfigParameters.from(directoryServerId, directoryServerPublicKey);

        try {
            ThreeDS2Service.INSTANCE.initialize(mActivity, configParameters, null, mUiCustomization);
        } catch (SDKAlreadyInitializedException e) {
            // Do nothing.
        }

        try {
            mTransaction = ThreeDS2Service.INSTANCE.createTransaction(null, null);
        } catch (SDKNotInitializedException e) {
            throw ThreeDS2Exception.from("Transaction creation failure, 3DS service isn't initialized.", e);
        }

        AuthenticationRequestParameters authenticationRequestParameters = mTransaction.getAuthenticationRequestParameters();
        Fingerprint fingerprint = new Fingerprint(authenticationRequestParameters);

        try {
            return Base64Coder.encode(fingerprint);
        } catch (JSONException e) {
            throw ThreeDS2Exception.from("Fingerprint encoding failure.", e);
        }
    }

    /**
     * Presents a challenge with a default challenge timeout of 10 minutes.
     * <p/>
     *
     * @param encodedChallengeToken The challenge token, as received from the Checkout API.
     */
    public void presentChallenge(@NonNull String encodedChallengeToken) throws ThreeDS2Exception {
        presentChallenge(encodedChallengeToken, DEFAULT_CHALLENGE_TIME_OUT);
    }

    /**
     * Presents a challenge.
     * <p/>
     *
     * @param encodedChallengeToken The challenge token, as received from the Checkout API.
     * @param challengeTimeOut      The challenge timeout in minutes, the default is 10 minutes, minimum is 5 minutes.
     */
    @SuppressWarnings("WeakerAccess")
    public void presentChallenge(@NonNull String encodedChallengeToken, int challengeTimeOut) throws ThreeDS2Exception {
        Challenge challenge;
        try {
            challenge = Base64Coder.decode(encodedChallengeToken, Challenge.class);
        } catch (JSONException e) {
            throw ThreeDS2Exception.from("Challenge token decoding failure.", e);
        }

        ChallengeParameters challengeParameters = createChallengeParameters(challenge);

        mTransaction.doChallenge(mActivity, challengeParameters, mListenerDelegate, challengeTimeOut);
    }

    // TODO: 21/11/2018 replace the release with lifecycle aware logic.
    public void release() {
        if (mTransaction != null) {
            mTransaction.close();
            mTransaction = null;
        }

        try {
            ThreeDS2Service.INSTANCE.cleanup(mActivity);
        } catch (SDKNotInitializedException e) {
            // Do nothing.
        }

        mActivity = null;
        mListenerDelegate = null;
    }

    @NonNull
    private ChallengeParameters createChallengeParameters(@NonNull Challenge challenge) {
        ChallengeParameters challengeParameters = new ChallengeParameters();
        challengeParameters.set3DSServerTransactionID(challenge.getThreeDSServerTransID());
        challengeParameters.setAcsTransactionID(challenge.getAcsTransID());
        challengeParameters.setAcsRefNumber(challenge.getAcsReferenceNumber());
        challengeParameters.setAcsSignedContent(challenge.getAcsSignedContent());

        return challengeParameters;
    }

    private final class ListenerDelegate implements ChallengeStatusReceiver {

        private static final String PROTOCOL_ERROR_FORMAT = "Error [code: %s, description: %s, details: %s]";

        private static final String RUNTIME_ERROR_FORMAT = "Error [code: %s, message: %s]";

        private final AuthenticationListener mDelegate;

        /**
         * Initializes the ListenerDelegate.
         */
        ListenerDelegate(@NonNull AuthenticationListener listener) {
            mDelegate = listener;
        }

        /**
         * Get ChallengeResult from CompletionEvent.
         */
        @Override
        public void completed(CompletionEvent completionEvent) {
            try {
                ChallengeResult challengeResult = ChallengeResultImpl.from(completionEvent);
                mDelegate.onSuccess(challengeResult);
            } catch (JSONException e) {
                mDelegate.onFailure(ThreeDS2Exception.from("Challenge result creation failure.", e));
            }
        }

        @Override
        public void cancelled() {
            mDelegate.onFailure(ThreeDS2Exception.from("Challenge was canceled."));
        }

        @Override
        public void timedout() {
            mDelegate.onFailure(ThreeDS2Exception.from("Challenge was timed out."));
        }

        /**
         * Generate error from ProtocolErrorEvent.
         */
        @Override
        public void protocolError(ProtocolErrorEvent protocolErrorEvent) {
            ErrorMessage errorMessage = protocolErrorEvent.getErrorMessage();

            String message = String.format(PROTOCOL_ERROR_FORMAT,
                    errorMessage.getErrorCode(),
                    errorMessage.getErrorDescription(),
                    errorMessage.getErrorDetails());

            mDelegate.onFailure(ThreeDS2Exception.from(message));
        }

        /**
         * Generate error from RuntimeErrorEvent.
         */
        @Override
        public void runtimeError(RuntimeErrorEvent runtimeErrorEvent) {
            String message = String.format(RUNTIME_ERROR_FORMAT,
                    runtimeErrorEvent.getErrorCode(),
                    runtimeErrorEvent.getErrorMessage());

            mDelegate.onFailure(ThreeDS2Exception.from(message));
        }
    }

    public interface AuthenticationListener {
        /**
         * Invoked on challenge finish without a failure.
         * <p/>
         *
         * @param challengeResult {@link ChallengeResult} contains the challlgen authentication state and payload.
         */
        void onSuccess(@NonNull ChallengeResult challengeResult);

        /**
         * Invoked on challenge failure.
         * <p/>
         *
         * @param e {@link ThreeDS2Exception} contains the failure metadata.
         */
        void onFailure(@NonNull ThreeDS2Exception e);
    }
}
