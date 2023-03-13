/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 7/12/2022.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import android.app.Activity
import androidx.annotation.DrawableRes
import com.adyen.authentication.AdyenAuthentication
import com.adyen.authentication.AuthenticationLauncher
import com.adyen.checkout.adyen3ds2.model.DelegatedAuthenticationResult
import com.adyen.checkout.adyen3ds2.model.DelegatedAuthenticationRegistrationResult
import com.adyen.checkout.components.core.internal.ui.model.TimerData
import kotlinx.coroutines.flow.Flow

interface DelegatedAuthenticationProvidingDelegate {

    val authenticationTimerFlow: Flow<TimerData>

    fun initAdyenAuthentication(authenticationLauncher: AuthenticationLauncher)

    fun getAdyenAuthentication(): AdyenAuthentication?

    fun getRegistrationSdkInput(): String?

    fun onRegistrationResult(result: DelegatedAuthenticationRegistrationResult)

    fun onRegistrationFailed()

    fun getAuthenticationSdkInput(): String?

    fun onAuthenticationResult(result: DelegatedAuthenticationResult, activity: Activity)

    fun onAuthenticationFailed(activity: Activity?)

    @DrawableRes
    fun getMerchantLogo(): Int?
}
