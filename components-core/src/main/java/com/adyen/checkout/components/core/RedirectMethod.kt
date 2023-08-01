/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/8/2023.
 */

package com.adyen.checkout.components.core

sealed class RedirectMethod {

    /**
     * The redirect action was opened in a browser. Note that the browser might open another app if it sees fit.
     */
    object Browser : RedirectMethod()

    /**
     * The redirect was opened with [Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs/). Note that
     * Custom Tabs may open the browser or another app if it sees fit.
     */
    object CustomTabs : RedirectMethod()

    /**
     *  The redirect was opened in an external app. This could for example be a banking app, but it cannot be a browser.
     */
    object ExternalApp : RedirectMethod()
}
