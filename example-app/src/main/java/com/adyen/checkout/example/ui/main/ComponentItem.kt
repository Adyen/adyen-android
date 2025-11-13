package com.adyen.checkout.example.ui.main

import androidx.annotation.StringRes
import com.adyen.checkout.example.R

internal sealed class ComponentItem {

    abstract val stringResource: Int
    abstract val arguments: List<String>?

    data class Title(
        @StringRes override val stringResource: Int,
        override val arguments: List<String>? = null
    ) : ComponentItem()

    sealed class Entry(
        @StringRes override val stringResource: Int,
        override val arguments: List<String>? = null
    ) : ComponentItem() {
        object DropIn : Entry(R.string.drop_in_entry)
        object DropInWithSession : Entry(R.string.drop_in_with_session_entry)
        object DropInWithCustomSession : Entry(R.string.drop_in_with_session_custom_entry)
        object Bacs : Entry(R.string.bacs_component_entry)
        object Blik : Entry(R.string.blik_component_entry)
        object Card : Entry(R.string.card_component_entry)
        object Klarna : Entry(R.string.klarna_component_entry)
        object PayPal : Entry(R.string.paypal_component_entry)
        data class Instant(private val paymentMethodType: String) :
            Entry(R.string.instant_component_entry, listOf(paymentMethodType))

        object CardWithSession : Entry(R.string.card_component_with_session_entry)
        object CardWithSessionTakenOver : Entry(R.string.card_component_with_session_taken_over_entry)
        object GiftCard : Entry(R.string.gift_card_component_entry)
        object GiftCardWithSession : Entry(R.string.gift_card_with_session_component_entry)
        object GooglePay : Entry(R.string.google_pay_component_entry)
        object GooglePayWithSession : Entry(R.string.google_pay_with_session_component_entry)
        data object V6 : Entry(R.string.v6_entry)
        data object V6Sessions : Entry(R.string.v6_entry)
        data object V6DropIn : Entry(R.string.v6_entry)
        data object V6DropInWithSession : Entry(R.string.v6_entry)
    }
}
