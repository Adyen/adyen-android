/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/8/2023.
 */

package com.adyen.checkout.dropin.old

/**
 * Represents the data shown in the dialog when an error occurs.
 *
 * @param title The title displayed in the dialog. If not provided a generic error title will be shown.
 * @param message The message displayed in the dialog. If not provided a generic error message will be shown.
 */
data class ErrorDialog(
    val title: String? = null,
    val message: String? = null,
)

/**
 * Represents the data shown in the dialog when the payment flow is finished.
 *
 * @param title The title displayed in the dialog.
 * @param message The message displayed in the dialog.
 */
data class FinishedDialog(
    val title: String,
    val message: String,
)
