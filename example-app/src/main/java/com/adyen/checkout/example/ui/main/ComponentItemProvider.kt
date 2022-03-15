package com.adyen.checkout.example.ui.main

internal object ComponentItemProvider {

    fun getComponentItems() = listOf(
        ComponentItem.Title("Drop In"),
        ComponentItem.Entry.DropIn,
        ComponentItem.Title("Components"),
        ComponentItem.Entry.Card,
    )
}
