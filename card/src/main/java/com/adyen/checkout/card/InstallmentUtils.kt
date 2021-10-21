package com.adyen.checkout.card

import android.content.Context
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.model.payments.request.Installments

object InstallmentUtils {

    fun makeInstallmentOptions(configuration: InstallmentConfiguration?, cardType: CardType?, isCardTypeReliable: Boolean): List<InstallmentModel> {
        val hasCardBasedInstallmentOptions = configuration?.cardBasedOptions != null
        val hasDefaultInstallmentOptions = configuration?.defaultOptions != null
        val hasOptionsForCardType = hasCardBasedInstallmentOptions && isCardTypeReliable && (configuration?.cardBasedOptions?.any { it.cardType == cardType } ?: false)

        return when {
            hasOptionsForCardType -> {
                makeInstallmentModelList(configuration?.cardBasedOptions?.firstOrNull { it.cardType == cardType }!!)
            }
            hasDefaultInstallmentOptions -> {
                makeInstallmentModelList(configuration?.defaultOptions!!)
            }
            else -> {
                emptyList()
            }
        }
    }

    private fun makeInstallmentModelList(installmentOptions: InstallmentOptions): List<InstallmentModel> {
        val installmentOptionsList = mutableListOf<InstallmentModel>()
        val oneTimeOption = InstallmentModel(
            textResId = R.string.checkout_card_installments_option_one_time,
            value = null,
            option = InstallmentOption.ONE_TIME
        )
        installmentOptionsList.add(oneTimeOption)

        if (installmentOptions.includeRevolving) {
            val revolvingOption = InstallmentModel(
                textResId = R.string.checkout_card_installments_option_revolving,
                value = 1,
                option = InstallmentOption.REVOLVING
            )
            installmentOptionsList.add(revolvingOption)
        }

        val regularOptions = installmentOptions.values.map {
            InstallmentModel(
                textResId = R.string.checkout_card_installments_option_regular,
                value = it,
                option = InstallmentOption.REGULAR
            )
        }
        installmentOptionsList.addAll(regularOptions)
        return installmentOptionsList
    }

    fun getTextForInstallmentOption(context: Context, installmentModel: InstallmentModel?): String {
        return when (installmentModel?.option) {
            InstallmentOption.REGULAR -> context.getString(installmentModel.textResId, installmentModel.value)
            InstallmentOption.REVOLVING, InstallmentOption.ONE_TIME -> context.getString(installmentModel.textResId)
            else -> ""
        }
    }

    fun makeInstallmentModelObject(installmentModel: InstallmentModel?): Installments? {
        return when (installmentModel?.option) {
            InstallmentOption.REGULAR, InstallmentOption.REVOLVING -> {
                Installments(installmentModel.option.type, installmentModel.value)
            }
            else -> null
        }

    }
}
