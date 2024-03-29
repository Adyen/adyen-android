/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/1/2021.
 */
package com.adyen.checkout.cse

/**
 * Class containing raw card data that needs to be encrypted.
 * Use [Builder] to instantiate and [CardEncrypter] to encrypt.
 */
class UnencryptedCard internal constructor(
    val number: String?,
    val expiryMonth: String?,
    val expiryYear: String?,
    val cvc: String?,
    val cardHolderName: String?,
) {

    /**
     * Builder for [UnencryptedCard].
     */
    class Builder {
        private var number: String? = null
        private var expiryMonth: String? = null
        private var expiryYear: String? = null
        private var cardHolderName: String? = null
        private var cvc: String? = null

        /**
         * Set the optional card number.
         *
         * @param number The card number.
         * @return The Builder instance.
         */
        fun setNumber(number: String): Builder {
            this.number = removeWhiteSpaces(number)
            return this
        }

        /**
         * Set the optional expiry date.
         *
         * @param expiryMonth The expiry month e.g. "1" or "01" for January.
         * @param expiryYear The expiry year e.g. "2021".
         * @return The Builder instance.
         */
        fun setExpiryDate(expiryMonth: String, expiryYear: String): Builder {
            this.expiryMonth = removeWhiteSpaces(expiryMonth)
            this.expiryYear = removeWhiteSpaces(expiryYear)
            return this
        }

        /**
         * Set the optional card security code.
         *
         * @param cvc The card security code.
         * @return The Builder instance.
         */
        fun setCvc(cvc: String): Builder {
            this.cvc = removeWhiteSpaces(cvc)
            return this
        }

        /**
         * Set the optional card holder name.
         *
         * @param holderName The holder name.
         * @return The Builder instance.
         */
        fun setHolderName(holderName: String): Builder {
            cardHolderName = trimAndRemoveMultipleWhiteSpaces(holderName)
            return this
        }

        /**
         * Builds the given [UnencryptedCard] object.
         *
         * @return The [UnencryptedCard] object.
         */
        @Throws(NullPointerException::class, IllegalStateException::class)
        fun build(): UnencryptedCard {
            return UnencryptedCard(
                number = number,
                expiryMonth = expiryMonth,
                expiryYear = expiryYear,
                cvc = cvc,
                cardHolderName = cardHolderName,
            )
        }

        private fun removeWhiteSpaces(string: String?): String? {
            return string?.replace("\\s".toRegex(), "")
        }

        private fun trimAndRemoveMultipleWhiteSpaces(string: String?): String? {
            return string?.trim { it <= ' ' }?.replace("\\s{2,}".toRegex(), " ")
        }
    }
}
