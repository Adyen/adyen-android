/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adyen.checkout.base.ComponentAvailableCallback
import com.adyen.checkout.base.component.Configuration
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.checkComponentAvailability
import com.adyen.checkout.dropin.ui.paymentmethods.GenericStoredModel
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodModel
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodsListModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredCardModel
import com.adyen.checkout.dropin.ui.paymentmethods.StoredPaymentMethodModel

class DropInViewModel(application: Application) : AndroidViewModel(application), ComponentAvailableCallback<Configuration> {

    companion object {
        val TAG = LogUtil.getTag()
    }

    private val paymentMethodsMutableLiveData: MutableLiveData<PaymentMethodsListModel> = MutableLiveData()
    val paymentMethodsLiveData: LiveData<PaymentMethodsListModel> = paymentMethodsMutableLiveData

    var paymentMethodsApiResponse: PaymentMethodsApiResponse = PaymentMethodsApiResponse()
        set(value) {
            if (value != paymentMethodsApiResponse) {
                field = value
                onPaymentMethodsResponseChanged(value.paymentMethods.orEmpty(), value.storedPaymentMethods.orEmpty())
            }
        }

    lateinit var dropInConfiguration: DropInConfiguration

    private var availabilitySum = 0
    private var availabilitySkipSum = 0
    private var availabilityChecksum = 0

    private val storedPaymentMethodsList = mutableListOf<StoredPaymentMethodModel>()
    private val paymentMethodsList = mutableListOf<PaymentMethodModel>()

    fun getStoredPaymentMethod(id: String): StoredPaymentMethod {
        return paymentMethodsApiResponse.storedPaymentMethods?.firstOrNull { it.id == id } ?: StoredPaymentMethod()
    }

    fun getPaymentMethod(type: String): PaymentMethod {
        return paymentMethodsApiResponse.paymentMethods?.firstOrNull { it.type == type } ?: PaymentMethod()
    }

    private fun onPaymentMethodsResponseChanged(paymentMethods: List<PaymentMethod>, storedPaymentMethods: List<StoredPaymentMethod>) {
        Logger.d(TAG, "onPaymentMethodsResponseChanged")
        setupStoredPaymentMethods(storedPaymentMethods)
        setupPaymentMethods(paymentMethods)
    }

    private fun setupStoredPaymentMethods(storedPaymentMethods: List<StoredPaymentMethod>) {
        storedPaymentMethodsList.clear()
        for (storedPaymentMethod in storedPaymentMethods) {
            val type = storedPaymentMethod.type
            val id = storedPaymentMethod.id
            if (type != null && id != null && PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type)) {
                // We don't check for availability on stored payment methods
                storedPaymentMethodsList.add(makeStoredModel(storedPaymentMethod))
            } else {
                Logger.e(TAG, "Unsupported stored payment method - $type - $id")
            }
        }
    }

    private fun makeStoredModel(storedPaymentMethod: StoredPaymentMethod): StoredPaymentMethodModel {
        return when (storedPaymentMethod.type) {
            PaymentMethodTypes.SCHEME -> {
                StoredCardModel(
                    storedPaymentMethod.id.orEmpty(),
                    storedPaymentMethod.brand.orEmpty(),
                    storedPaymentMethod.lastFour.orEmpty(),
                    storedPaymentMethod.expiryMonth.orEmpty(),
                    storedPaymentMethod.expiryYear.orEmpty()
                )
            }
            else -> GenericStoredModel(
                storedPaymentMethod.id.orEmpty(),
                storedPaymentMethod.type.orEmpty(),
                storedPaymentMethod.name.orEmpty()
            )
        }
    }

    private fun setupPaymentMethods(paymentMethods: List<PaymentMethod>) {
        // We can't remove availability callbacks so just for safety let's crash in case of a concurrency issue.
        if (availabilityChecksum != 0) {
            throw CheckoutException("Concurrency error. Cannot update Payment methods list because availability is still being checked.")
        }

        // Reset variables
        availabilitySum = 0
        availabilitySkipSum = 0
        availabilityChecksum = paymentMethods.size
        paymentMethodsList.clear()

        for (paymentMethod in paymentMethods) {
            val type = paymentMethod.type
            when {
                type == null -> {
                    throw CheckoutException("PaymentMethod type is null")
                }
                PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) -> {
                    Logger.v(TAG, "Supported payment method: $type")
                    // We assume payment method is available and remove it later when the callback comes
                    // this is the overwhelming majority of cases, and we keep the list ordered this way.
                    paymentMethodsList.add(
                        PaymentMethodModel(type, paymentMethod.name.orEmpty())
                    )
                    checkComponentAvailability(getApplication(), paymentMethod, dropInConfiguration, this)
                }
                else -> {
                    availabilitySkipSum++
                    if (PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.contains(type)) {
                        Logger.e(TAG, "PaymentMethod not yet supported - $type")
                    } else {
                        Logger.d(TAG, "No details required - $type")
                        paymentMethodsList.add(
                            PaymentMethodModel(type, paymentMethod.name.orEmpty())
                        )
                    }
                    // If last payment method is redirect list might be ready now
                    checkIfListIsReady()
                }
            }
        }
    }

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod, config: Configuration?) {
        Logger.d(TAG, "onAvailabilityResult - ${paymentMethod.type}: $isAvailable")

        availabilitySum++

        if (!isAvailable) {
            Logger.e(TAG, "${paymentMethod.type} NOT AVAILABLE")
            paymentMethodsList.removeAll { it.type == paymentMethod.type }
        }
        checkIfListIsReady()
    }

    private fun checkIfListIsReady() {
        // All payment methods availability have been returned or skipped.
        if ((availabilitySum + availabilitySkipSum) == availabilityChecksum) {
            // Reset variables
            availabilitySum = 0
            availabilitySkipSum = 0
            availabilityChecksum = 0

            onPaymentMethodsReady()
        }
    }

    private fun onPaymentMethodsReady() {
        Logger.d(TAG, "onPaymentMethodsReady: ${storedPaymentMethodsList.size} - ${paymentMethodsList.size}")
        val paymentMethodsListModel = PaymentMethodsListModel(storedPaymentMethodsList, paymentMethodsList)
        paymentMethodsMutableLiveData.value = paymentMethodsListModel
    }
}
