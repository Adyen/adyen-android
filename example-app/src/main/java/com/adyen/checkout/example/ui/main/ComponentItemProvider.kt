package com.adyen.checkout.example.ui.main

import com.adyen.checkout.example.R

internal object ComponentItemProvider {

    fun getComponentItems() = listOf(
        ComponentItem.Title(R.string.drop_in_title),
        ComponentItem.Entry.DropIn,
        ComponentItem.Entry.DropInWithSession,
        ComponentItem.Entry.DropInWithCustomSession,
        ComponentItem.Title(R.string.components_title),
        ComponentItem.Entry.Card,
    )
}
