/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 7/12/2022.
 */

package com.adyen.checkout.adyen3ds2

import android.app.Activity
import com.adyen.authentication.AdyenAuthentication
import com.adyen.authentication.AuthenticationLauncher
import com.adyen.checkout.adyen3ds2.model.DAAuthenticationResult
import com.adyen.checkout.adyen3ds2.model.DARegistrationResult
import com.adyen.checkout.components.status.model.TimerData
import kotlinx.coroutines.flow.Flow

interface DAProvidingDelegate {

    fun initAdyenAuthentication(authenticationLauncher: AuthenticationLauncher)

    fun getAdyenAuthentication(): AdyenAuthentication?

    fun getRegistrationSdkInput(): String?

    fun onRegistrationResult(result: DARegistrationResult)

    fun onRegistrationFailed()

    fun getAuthenticationSdkInput(): String?

    fun onAuthenticationResult(result: DAAuthenticationResult, activity: Activity)

    fun onAuthenticationFailed(activity: Activity?)

    fun getAuthenticationTimerFlow(): Flow<TimerData>
}
