/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/11/2020.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adyen.checkout.components.ComponentAvailableCallback
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.checkPaymentMethodAvailability
import com.adyen.checkout.dropin.ui.stored.makeStoredModel

class PaymentMethodsListViewModel(
    application: Application,
    paymentMethods: List<PaymentMethod>,
    storedPaymentMethods: List<StoredPaymentMethod>,
    val dropInConfiguration: DropInConfiguration
) : AndroidViewModel(application), ComponentAvailableCallback<Configuration> {

    private val paymentMethodsMutableLiveData: MutableLiveData<PaymentMethodsListModel> = MutableLiveData()
    val paymentMethodsLiveData: LiveData<PaymentMethodsListModel> = paymentMethodsMutableLiveData

    private var availabilitySum = 0
    private var availabilitySkipSum = 0
    private var availabilityChecksum = 0

    private val storedPaymentMethodsList = mutableListOf<StoredPaymentMethodModel>()
    private val paymentMethodsList = mutableListOf<PaymentMethodModel>()

    init {
        Logger.d(TAG, "onPaymentMethodsResponseChanged")
        setupStoredPaymentMethods(storedPaymentMethods)
        setupPaymentMethods(paymentMethods)
    }

    private fun setupStoredPaymentMethods(storedPaymentMethods: List<StoredPaymentMethod>) {
        storedPaymentMethodsList.clear()
        for (storedPaymentMethod in storedPaymentMethods) {
            if (isStoredPaymentSupported(storedPaymentMethod)) {
                // We don't check for availability on stored payment methods
                storedPaymentMethodsList.add(makeStoredModel(storedPaymentMethod))
            } else {
                Logger.e(TAG, "Unsupported stored payment method - ${storedPaymentMethod.type} : ${storedPaymentMethod.name}")
            }
        }
    }

    private fun isStoredPaymentSupported(storedPaymentMethod: StoredPaymentMethod): Boolean {
        return !storedPaymentMethod.type.isNullOrEmpty() &&
            !storedPaymentMethod.id.isNullOrEmpty() &&
            PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(storedPaymentMethod.type) &&
            storedPaymentMethod.isEcommerce
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
                    checkPaymentMethodAvailability(getApplication(), paymentMethod, dropInConfiguration, this)
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

    companion object {
        val TAG = LogUtil.getTag()
    }
}
