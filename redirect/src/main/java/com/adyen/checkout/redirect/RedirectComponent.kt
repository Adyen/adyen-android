/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/4/2019.
 */

package com.adyen.checkout.redirect;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;

import com.adyen.checkout.components.ActionComponentProvider;
import com.adyen.checkout.components.base.BaseActionComponent;
import com.adyen.checkout.components.base.IntentHandlingComponent;
import com.adyen.checkout.components.model.payments.response.Action;
import com.adyen.checkout.components.model.payments.response.RedirectAction;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.exception.ComponentException;

import org.json.JSONObject;

public final class RedirectComponent extends BaseActionComponent<RedirectConfiguration> implements IntentHandlingComponent {
    public static final ActionComponentProvider<RedirectComponent, RedirectConfiguration> PROVIDER = new RedirectComponentProvider();

    private final RedirectDelegate mRedirectDelegate;

    public RedirectComponent(
            @NonNull SavedStateHandle savedStateHandle,
            @NonNull Application application,
            @NonNull RedirectConfiguration configuration,
            @NonNull RedirectDelegate redirectDelegate
    ) {
        super(savedStateHandle, application, configuration);
        mRedirectDelegate = redirectDelegate;
    }

    /**
     * Returns the suggested value to be used as the `returnUrl` value in the payments/ call.
     *
     * @param context The context provides the package name which constitutes part of the ReturnUrl
     * @return The suggested `returnUrl` to be used. Consists of {@link RedirectUtil#REDIRECT_RESULT_SCHEME} + App package name.
     */
    @NonNull
    public static String getReturnUrl(@NonNull Context context) {
        return RedirectUtil.REDIRECT_RESULT_SCHEME + context.getPackageName();
    }

    @Override
    public boolean canHandleAction(@NonNull Action action) {
        return PROVIDER.canHandleAction(action);
    }

    @Override
    protected void handleActionInternal(@NonNull Activity activity, @NonNull Action action) throws ComponentException {
        final RedirectAction redirectAction = (RedirectAction) action;
        mRedirectDelegate.makeRedirect(activity, redirectAction);
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the {@link Intent#getData()} and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received {@link Intent}.
     */
    @Override
    public void handleIntent(@NonNull Intent intent) {
        try {
            final JSONObject parsedResult = mRedirectDelegate.handleRedirectResponse(intent.getData());
            notifyDetails(parsedResult);
        } catch (CheckoutException e) {
            notifyException(e);
        }
    }
}
