/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/06/2018.
 */

package com.adyen.checkout.core.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.internal.model.WeChatPayQrRedirectDataImpl;

@ProvidedBy(WeChatPayQrRedirectDataImpl.class)
public interface WeChatPayQrRedirectData extends RedirectData {
    /**
     * @return The URL pointing to the QR code image that contains the {@link #getRedirectTargetUrl()}.
     */
    @NonNull
    String getQrCodeUrl();

    /**
     * @return The URL that the shopper needs to be redirected to, e.g. with a QR code or a link.
     */
    @NonNull
    String getRedirectTargetUrl();
}
