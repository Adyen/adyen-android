/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/11/2023.
 */

package com.adyen.checkout.example.ui.compose

import com.adyen.checkout.example.R

data class ResultState(
    val drawable: Int,
    val text: String,
) {

    companion object {
        val SUCCESS = ResultState(R.drawable.ic_result_success, "Payment successful!")
        val PENDING = ResultState(R.drawable.ic_result_pending, "Payment pending...")
        val FAILURE = ResultState(R.drawable.ic_result_failure, "Payment failed...")

        fun get(resultCode: String?): ResultState = when (resultCode) {
            "Authorised" -> SUCCESS
            "Pending",
            "Received" -> PENDING

            else -> FAILURE
        }
    }
}
