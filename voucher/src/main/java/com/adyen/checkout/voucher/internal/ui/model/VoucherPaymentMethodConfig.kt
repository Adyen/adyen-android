/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 17/3/2023.
 */

package com.adyen.checkout.voucher.internal.ui.model

import androidx.annotation.StringRes
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.util.DateUtils
import com.adyen.checkout.voucher.R
import com.adyen.checkout.voucher.internal.ui.VoucherComponentViewType
import java.util.Locale

internal enum class VoucherPaymentMethodConfig(
    val viewType: VoucherComponentViewType,
    @StringRes val introductionTextResource: Int?,
) {

    BACS(
        viewType = VoucherComponentViewType.SIMPLE_VOUCHER,
        introductionTextResource = R.string.checkout_voucher_introduction_bacs,
    ),
    BOLETO(
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        introductionTextResource = R.string.checkout_voucher_introduction,
    ),
    ECONTEXT(
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        introductionTextResource = R.string.checkout_voucher_introduction,
    ),
    MULTIBANCO(
        viewType = VoucherComponentViewType.FULL_VOUCHER,
        introductionTextResource = R.string.checkout_voucher_introduction,
    );

    companion object {

        fun getByPaymentMethodType(paymentMethodType: String?): VoucherPaymentMethodConfig? {
            return when (paymentMethodType) {
                PaymentMethodTypes.BACS -> BACS

                PaymentMethodTypes.BOLETOBANCARIO,
                PaymentMethodTypes.BOLETOBANCARIO_BANCODOBRASIL,
                PaymentMethodTypes.BOLETOBANCARIO_BRADESCO,
                PaymentMethodTypes.BOLETOBANCARIO_HSBC,
                PaymentMethodTypes.BOLETOBANCARIO_ITAU,
                PaymentMethodTypes.BOLETOBANCARIO_SANTANDER,
                PaymentMethodTypes.BOLETO_PRIMEIRO_PAY -> BOLETO

                PaymentMethodTypes.ECONTEXT_ATM,
                PaymentMethodTypes.ECONTEXT_ONLINE,
                PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN,
                PaymentMethodTypes.ECONTEXT_STORES -> ECONTEXT

                PaymentMethodTypes.MULTIBANCO -> MULTIBANCO

                else -> null
            }
        }
    }
}

internal fun VoucherPaymentMethodConfig.getInformationFields(action: VoucherAction, shopperLocale: Locale) =
    when (this) {
        VoucherPaymentMethodConfig.BOLETO -> listOfNotNull(
            createExpirationInformationField(action, shopperLocale),
        )

        VoucherPaymentMethodConfig.ECONTEXT -> listOfNotNull(
            createStoreNumberField(action),
            createExpirationInformationField(action, shopperLocale),
            createPhoneNumberField(action),
        )

        VoucherPaymentMethodConfig.MULTIBANCO -> listOfNotNull(
            createEntityInformationField(action),
            createExpirationInformationField(action, shopperLocale),
            createShopperReferenceField(action),
        )

        else -> null
    }

private fun createEntityInformationField(action: VoucherAction): VoucherInformationField? {
    val entity = action.entity ?: return null

    return VoucherInformationField(
        labelResId = R.string.checkout_voucher_expiration_entity,
        value = entity,
    )
}

private fun createStoreNumberField(action: VoucherAction): VoucherInformationField? {
    val storeNumber = action.collectionInstitutionNumber ?: return null
    return VoucherInformationField(R.string.checkout_voucher_collection_institution_number, value = storeNumber)
}

private fun createPhoneNumberField(action: VoucherAction): VoucherInformationField? {
    val phoneNumber = action.maskedTelephoneNumber ?: return null
    return VoucherInformationField(R.string.checkout_voucher_phone_number, value = phoneNumber)
}

private fun createExpirationInformationField(action: VoucherAction, shopperLocale: Locale): VoucherInformationField? {
    val expirationDate = action.expiresAt?.let { expiresAt ->
        DateUtils.formatStringDate(
            expiresAt,
            shopperLocale,
        )
    } ?: return null

    return VoucherInformationField(
        labelResId = R.string.checkout_voucher_expiration_date,
        value = expirationDate,
    )
}

private fun createShopperReferenceField(action: VoucherAction): VoucherInformationField? {
    val merchantReference = action.merchantReference ?: return null

    return VoucherInformationField(
        labelResId = R.string.checkout_voucher_shopper_reference,
        value = merchantReference,
    )
}
