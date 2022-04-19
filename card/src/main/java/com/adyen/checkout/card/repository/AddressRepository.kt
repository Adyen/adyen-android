/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/3/2022.
 */

package com.adyen.checkout.card.repository

import com.adyen.checkout.card.api.AddressConnection
import com.adyen.checkout.card.api.AddressDataType
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.components.api.suspendedCall
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONException
import java.io.IOException

class AddressRepository {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    suspend fun getAddressData(
        environment: Environment,
        dataType: AddressDataType,
        localeString: String,
        countryCode: String? = null
    ): List<AddressItem> {
        Logger.d(TAG, "getting address data")
        try {
            return AddressConnection(environment, dataType, localeString, countryCode).suspendedCall()
        } catch (e: IOException) {
            Logger.e(TAG, "AddressConnection Failed")
            throw CheckoutException("Unable to get address data.")
        } catch (e: JSONException) {
            Logger.e(TAG, "AddressConnection unexpected result")
            throw CheckoutException("Unable to get address data.")
        }
    }
}
