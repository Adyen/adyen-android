package com.adyen.checkout.example.ui.main

import com.adyen.checkout.example.R

internal object ComponentItemProvider {

    fun getDefaultItems() = listOf(
        ComponentItem.Title(R.string.drop_in_title),
        ComponentItem.Entry.DropIn,
        ComponentItem.Title(R.string.components_title),
        ComponentItem.Entry.Bacs,
        ComponentItem.Entry.Blik,
        ComponentItem.Entry.Card,
        ComponentItem.Entry.GiftCard,
        ComponentItem.Entry.Instant,
    )

    fun getSessionItems() = listOf(
        ComponentItem.Title(R.string.drop_in_title),
        ComponentItem.Entry.DropInWithSession,
        ComponentItem.Entry.DropInWithCustomSession,
        ComponentItem.Title(R.string.components_title),
        ComponentItem.Entry.CardWithSession,
        ComponentItem.Entry.CardWithSessionTakenOver,
    )
}
