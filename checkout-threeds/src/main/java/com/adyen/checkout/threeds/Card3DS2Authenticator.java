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

    private UiCustomization mUiCustomization;

    private Transaction mTransaction;

    /**
     * Initializes the 3DS2 card authenticator.
     * <p/>
     *
     * @param activity The current activity
     */
    public Card3DS2Authenticator(@NonNull Activity activity) {
        this(activity, null);
    }

    /**
     * Initializes the 3DS2 card authenticator.
     * <p/>
     *
     * @param activity        The current activity.
     * @param uiCustomization (optional) The {@link UiCustomization}, UI configuration information that is used to specify the UI layout and theme.
     */
    @SuppressWarnings("WeakerAccess")
    public Card3DS2Authenticator(@NonNull Activity activity, @Nullable UiCustomization uiCustomization) {
        mActivity = activity;
        mUiCustomization = uiCustomization;
    }

    /**
     * Creates a fingerprint using a fingerprint token received from the Checkout API.
     * <p/>
     *
     * @param encodedFingerprintToken The fingerprint token received from the Checkout API.
     * @param listener                {@link FingerprintListener} The fingerprint listener listens to fingerprint creation success or failure.
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public void createFingerprint(@NonNull String encodedFingerprintToken, @NonNull FingerprintListener listener) {
        try {
            FingerprintToken fingerprintToken = Base64Coder.decode(encodedFingerprintToken, FingerprintToken.class);
            createFingerprint(fingerprintToken.getDirectoryServerId(), fingerprintToken.getDirectoryServerPublicKey(), listener);
        } catch (JSONException e) {
            listener.onFailure(ThreeDS2Exception.from("Fingerprint token decoding failure.", e));
        }
    }

    /**
     * Creates a fingerprint using a directory server identifier and public key.
     * <p/>
     *
     * @param directoryServerId        The directory server identifier.
     * @param directoryServerPublicKey The directory server public key.
     * @param listener                 {@link FingerprintListener} The fingerprint listener listens to fingerprint creation success or failure.
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public void createFingerprint(
            @NonNull String directoryServerId,
            @NonNull String directoryServerPublicKey,
            @NonNull FingerprintListener listener
    ) {
        ConfigParameters configParameters = new AdyenConfigParameters.Builder(directoryServerId, directoryServerPublicKey).build();

        try {
            // TODO: 15/04/2019 make this call async.
            ThreeDS2Service.INSTANCE.initialize(mActivity, configParameters, null, mUiCustomization);
        } catch (SDKAlreadyInitializedException e) {
            // Do nothing.
        }

        try {
            closeTransaction();
            createTransaction();
        } catch (SDKNotInitializedException e) {
            listener.onFailure(ThreeDS2Exception.from("Transaction creation failure, 3DS service isn't initialized.", e));
        }

        AuthenticationRequestParameters authenticationRequestParameters = mTransaction.getAuthenticationRequestParameters();
        Fingerprint fingerprint = new Fingerprint(authenticationRequestParameters);

        try {
            String encodedFingerprint = Base64Coder.encode(fingerprint);
            listener.onSuccess(encodedFingerprint);
        } catch (JSONException e) {
            listener.onFailure(ThreeDS2Exception.from("Fingerprint encoding failure.", e));
        }
    }

    /**
     * Presents a challenge with a default challenge timeout of 10 minutes.
     * <p/>
     *
     * @param encodedChallengeToken The challenge token, as received from the Checkout API.
     * @param listener              {@link ChallengeListener} The challenge listener listens to challenge authentication success or failure.
     */
    public void presentChallenge(@NonNull String encodedChallengeToken, @NonNull ChallengeListener listener) throws ThreeDS2Exception {
        presentChallenge(encodedChallengeToken, DEFAULT_CHALLENGE_TIME_OUT, listener);
    }

    /**
     * Presents a challenge.
     * <p/>
     *
     * @param encodedChallengeToken The challenge token, as received from the Checkout API.
     * @param challengeTimeOut      The challenge timeout in minutes, the default is 10 minutes, minimum is 5 minutes.
     * @param listener              {@link ChallengeListener} The challenge listener listens to challenge authentication success or failure.
     */
    @SuppressWarnings("WeakerAccess")
    public void presentChallenge(
            @NonNull String encodedChallengeToken,
            int challengeTimeOut,
            @NonNull ChallengeListener listener
    ) throws ThreeDS2Exception {
        try {
            Challenge challenge = Base64Coder.decode(encodedChallengeToken, Challenge.class);
            ChallengeParameters challengeParameters = createChallengeParameters(challenge);
            mTransaction.doChallenge(mActivity, challengeParameters, new ListenerDelegate(listener), challengeTimeOut);
        } catch (JSONException e) {
            throw ThreeDS2Exception.from("Challenge token decoding failure.", e);
        }
    }

    /**
     * Releases the resources been held by the {@link Card3DS2Authenticator}.
     */
    // TODO: 21/11/2018 replace the release with lifecycle aware logic.
    public synchronized void release() {
        closeTransaction();

        try {
            ThreeDS2Service.INSTANCE.cleanup(mActivity);
        } catch (SDKNotInitializedException e) {
            // Do nothing.
        }

        mActivity = null;
    }

    /**
     * @return true if the {@link Card3DS2Authenticator} is released, otherwise false.
     */
    public synchronized boolean isReleased() {
        return mActivity == null;
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

    private void createTransaction() throws SDKNotInitializedException {
        mTransaction = ThreeDS2Service.INSTANCE.createTransaction(null, null);
    }

    private void closeTransaction() {
        if (mTransaction != null) {
            mTransaction.close();
            mTransaction = null;
        }
    }

    public interface FingerprintListener {
        /**
         * Invoked on fingerprint creation without a failure.
         * <p/>
         *
         * @param fingerprint contains the fingerprint data.
         */
        void onSuccess(@NonNull String fingerprint);

        /**
         * Invoked on fingerprint failure.
         * <p/>
         *
         * @param e {@link ThreeDS2Exception} contains the failure metadata.
         */
        void onFailure(@NonNull ThreeDS2Exception e);
    }

    public interface ChallengeListener {
        /**
         * Invoked on challenge finish without a failure.
         * <p/>
         *
         * @param challengeResult {@link ChallengeResult} contains the challenge authentication state and payload.
         */
        void onSuccess(@NonNull ChallengeResult challengeResult);

        /**
         * This method will be called when a user backs from a challenge.
         */
        void onCancel();

        /**
         * This method will be called on challenge timeout.<br>
         * The default timeout is 10 minutes, the minimum timout is 5 minutes.<br>
         * It is possible to change the challenge timeout by passing desirable timeout to the following method
         * {@link Card3DS2Authenticator#presentChallenge(String, int, ChallengeListener)}
         */
        void onTimeout();

        /**
         * Invoked on challenge failure.
         * <p/>
         *
         * @param e {@link ThreeDS2Exception} contains the failure metadata.
         */
        void onFailure(@NonNull ThreeDS2Exception e);
    }

    /**
     * Simple implementation of {@link ChallengeListener} provides empty implementation of optional
     * callback methods {@link ChallengeListener#onCancel()} and {@link ChallengeListener#onTimeout()}.
     */
    public abstract static class SimpleChallengeListener implements ChallengeListener {
        @Override
        public void onCancel() {
        }

        @Override
        public void onTimeout() {
        }
    }

    private final class ListenerDelegate implements ChallengeStatusReceiver {

        private static final String PROTOCOL_ERROR_FORMAT = "Error [code: %s, description: %s, details: %s]";

        private static final String RUNTIME_ERROR_FORMAT = "Error [code: %s, message: %s]";

        private final ChallengeListener mDelegate;

        /**
         * Initializes the ListenerDelegate.
         */
        ListenerDelegate(@NonNull ChallengeListener listener) {
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
            mDelegate.onCancel();
        }

        @Override
        public void timedout() {
            mDelegate.onTimeout();
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
}
