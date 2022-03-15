package com.adyen.checkout.example.ui.main

internal object ComponentItemProvider {

    fun getComponentItems() = listOf(
        ComponentItem.Title("Drop In"),
        ComponentItem.Entry("Start"),
        ComponentItem.Title("Components"),
        ComponentItem.Entry("Card"),
    )
}
