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
import android.net.Uri;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.ComponentError;
import com.adyen.checkout.base.DetailsComponentProvider;
import com.adyen.checkout.base.component.BaseActionComponent;
import com.adyen.checkout.base.component.DetailsComponentProviderImpl;
import com.adyen.checkout.base.model.payments.response.Action;
import com.adyen.checkout.base.model.payments.response.RedirectAction;
import com.adyen.checkout.core.exeption.CheckoutException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.core.util.StringUtil;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public final class RedirectComponent extends BaseActionComponent {
    private static final String TAG = LogUtil.getTag();

    public static final DetailsComponentProvider<RedirectComponent> PROVIDER =
            new DetailsComponentProviderImpl<>(RedirectComponent.class);

    public RedirectComponent(@NonNull Application application) {
        super(application);
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

    /**
     * Make a redirect from the provided Activity to the target of the Redirect object.
     *
     * @param activity The Activity starting the redirect.
     * @param redirectAction The object from the server response defining where to redirect to.
     */
    public static void makeRedirect(@NonNull Activity activity, @NonNull RedirectAction redirectAction) throws CheckoutException {
        Logger.d(TAG, "makeRedirect - " + redirectAction.getUrl());
        if (StringUtil.hasContent(redirectAction.getUrl())) {
            final Uri redirectUri = Uri.parse(redirectAction.getUrl());
            final Intent redirectIntent = RedirectUtil.createRedirectIntent(activity, redirectUri);
            activity.startActivity(redirectIntent);
        } else {
            throw new CheckoutException("Redirect URI is empty.");
        }

    }

    @Override
    @NonNull
    protected List<String> getSupportedActionTypes() {
        final String[] supportedCodes = {RedirectAction.ACTION_TYPE};
        return Collections.unmodifiableList(Arrays.asList(supportedCodes));
    }

    @Override
    public void handleActionInternal(@NonNull Activity activity, @NonNull Action action) {
        final RedirectAction redirectAction = (RedirectAction) action;
        try {
            makeRedirect(activity, redirectAction);
        } catch (CheckoutException e) {
            notifyError(new ComponentError(e));
        }
    }

    public void handleRedirectResponse(@NonNull Uri data) {
        final JSONObject parsedResult = RedirectUtil.parseRedirectResult(data);
        notifyDetails(parsedResult);
    }


}
