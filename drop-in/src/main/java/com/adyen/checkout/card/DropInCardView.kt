/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 30/8/2019.
 */

package com.adyen.checkout.card

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.dropin.databinding.ViewCardComponentDropinBinding

internal class DropInCardView : AdyenLinearLayout<CardOutputData, CardConfiguration, CardComponentState, CardComponent>, Observer<CardOutputData> {

    val binding: ViewCardComponentDropinBinding
    private lateinit var mCardListAdapter: CardListAdapter

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        orientation = VERTICAL
        binding = ViewCardComponentDropinBinding.inflate(LayoutInflater.from(context), this)
    }

    override fun initView() {
        // nothing
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        // drop-in is localized by the activity context already
    }

    override fun onComponentAttached() {
        if (!component.isStoredPaymentMethod()) {
            mCardListAdapter = CardListAdapter(
                ImageLoader.getInstance(context, component.configuration.environment),
                component.configuration.supportedCardTypes
            )
            binding.recyclerViewCardList.adapter = mCardListAdapter
        }
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        binding.cardView.attach(component, lifecycleOwner)
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onChanged(cardOutputData: CardOutputData?) {
        cardOutputData?.let {
            if (!component.isStoredPaymentMethod()) {
                mCardListAdapter.setFilteredCard(component.filteredSupportedCards)
            }
        }
    }

    override fun isConfirmationRequired(): Boolean {
        return true
    }

    override fun highlightValidationErrors() {
        binding.cardView.highlightValidationErrors()
    }
}
