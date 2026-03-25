/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view.brand

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RestrictTo
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.R
import com.adyen.checkout.card.databinding.BrandViewBinding
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.Environment
import com.adyen.checkout.ui.core.internal.ui.loadLogo

/**
 * BrandView for card input field.
 *
 * Handles three display states:
 * - Single brand: one icon displayed
 * - Dual brand (non-selectable): two icons displayed side-by-side
 * - Dual brand (selectable): two icons with selection backgrounds, clickable
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class BrandView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr,
    ) {

    private var brandSelectionListener: BrandSelectionListener? = null

    private var cardBrandAdapter: CardBrandAdapter? = null

    private val binding: BrandViewBinding = BrandViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        val spacing = resources.getDimensionPixelSize(R.dimen.brand_logo_item_spacing)
        binding.recyclerViewBrandsList.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    if (position > 0) {
                        outRect.left = spacing
                    }
                }
            },
        )
    }

    fun update(detectedCardTypes: List<DetectedCardType>, dualBrandData: DualBrandData?, environment: Environment) {
        when {
            detectedCardTypes.isEmpty() -> showSingleBrandPlaceholder()
            dualBrandData != null -> showBrandsList(dualBrandData)
            else -> showSingleBrand(detectedCardTypes.first(), environment)
        }
    }

    fun setOnBrandSelectionListener(listener: BrandSelectionListener?) {
        this.brandSelectionListener = listener
    }

    private fun showSingleBrandPlaceholder() {
        binding.imageViewSingleBrand.isVisible = true
        binding.recyclerViewBrandsList.isVisible = false
        binding.imageViewSingleBrand.setImageResource(R.drawable.ic_card)
    }

    private fun showSingleBrand(detectedCardType: DetectedCardType, environment: Environment) {
        binding.imageViewSingleBrand.isVisible = true
        binding.recyclerViewBrandsList.isVisible = false
        binding.imageViewSingleBrand.loadLogo(
            environment = environment,
            txVariant = detectedCardType.cardBrand.txVariant,
        )
    }

    private fun showBrandsList(dualBrandData: DualBrandData) {
        val selectable = dualBrandData.selectable
        binding.imageViewSingleBrand.isVisible = false
        binding.recyclerViewBrandsList.isVisible = true
        binding.recyclerViewBrandsList.setBackgroundResource(
            if (selectable) R.drawable.bg_selectable_brand_list else 0,
        )
        val adapter = getOrCreateAdapter(selectable)
        adapter.submitList(dualBrandData.brandOptions)
    }

    private fun getOrCreateAdapter(selectable: Boolean): CardBrandAdapter {
        if (cardBrandAdapter == null || cardBrandAdapter?.isSelectable != selectable) {
            cardBrandAdapter = CardBrandAdapter(selectable) { cardBrandItem ->
                brandSelectionListener?.onBrandSelected(cardBrandItem.brand)
            }
            binding.recyclerViewBrandsList.adapter = cardBrandAdapter
        }
        return cardBrandAdapter!!
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun interface BrandSelectionListener {
        fun onBrandSelected(cardBrand: CardBrand)
    }
}
