package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType

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
            text = "one time", //TODO translations
            value = null,
            option = InstallmentOption.ONE_TIME
        )
        installmentOptionsList.add(oneTimeOption)

        if (installmentOptions.includeRevolving) {
            val revolvingOption = InstallmentModel(
                text = "revolving", //TODO translations
                value = 1,
                option = InstallmentOption.REVOLVING
            )
            installmentOptionsList.add(revolvingOption)
        }

        val regularOptions = installmentOptions.values.map {
            InstallmentModel(
                text = "regular $it",//TODO translations
                value = it,
                option = InstallmentOption.REGULAR
            )
        }
        installmentOptionsList.addAll(regularOptions)
        return installmentOptionsList
    }
}
