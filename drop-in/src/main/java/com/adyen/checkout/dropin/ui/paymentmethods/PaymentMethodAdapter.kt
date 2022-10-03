/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.view.AdyenSwipeToRevealLayout
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.components.util.DateUtils
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.databinding.PaymentMethodsListHeaderBinding
import com.adyen.checkout.dropin.databinding.PaymentMethodsListItemBinding
import com.adyen.checkout.dropin.databinding.PaymentMethodsListNoteBinding
import com.adyen.checkout.dropin.databinding.RemovablePaymentMethodsListItemBinding
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.GIFT_CARD_PAYMENT_METHOD
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.PAYMENT_METHOD
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.PAYMENT_METHODS_HEADER
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.PAYMENT_METHODS_NOTE
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.STORED_PAYMENT_METHOD

@SuppressWarnings("TooManyFunctions")
class PaymentMethodAdapter @JvmOverloads constructor(
    private val paymentMethods: MutableList<PaymentMethodListItem>,
    private val imageLoader: ImageLoader,
    private val onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback? = null,
    private val onStoredPaymentRemovedCallback: OnStoredPaymentRemovedCallback? = null,
    private val onUnderlayExpandListener: ((AdyenSwipeToRevealLayout) -> Unit)? = null
) : RecyclerView.Adapter<PaymentMethodAdapter.BaseViewHolder>() {

    constructor(
        paymentMethods: Collection<PaymentMethodListItem>,
        imageLoader: ImageLoader,
        onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback? = null,
        onStoredPaymentRemovedCallback: OnStoredPaymentRemovedCallback? = null,
        onUnderlayExpandListener: ((AdyenSwipeToRevealLayout) -> Unit)? = null
    ) : this(
        paymentMethods.toMutableList(),
        imageLoader,
        onPaymentMethodSelectedCallback,
        onStoredPaymentRemovedCallback,
        onUnderlayExpandListener
    )

    @SuppressLint("NotifyDataSetChanged")
    fun updatePaymentMethods(paymentMethods: List<PaymentMethodListItem>) {
        this.paymentMethods.clear()
        this.paymentMethods.addAll(paymentMethods)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            PAYMENT_METHODS_HEADER -> HeaderVH(
                PaymentMethodsListHeaderBinding.inflate(inflater, parent, false)
            )
            STORED_PAYMENT_METHOD -> StoredPaymentMethodVH(
                RemovablePaymentMethodsListItemBinding.inflate(inflater, parent, false),
                imageLoader,
                onUnderlayExpandListener
            )
            PAYMENT_METHOD -> PaymentMethodVH(
                PaymentMethodsListItemBinding.inflate(inflater, parent, false),
                imageLoader
            )
            GIFT_CARD_PAYMENT_METHOD -> GiftCardPaymentMethodVH(
                PaymentMethodsListItemBinding.inflate(inflater, parent, false),
                imageLoader
            )
            PAYMENT_METHODS_NOTE -> NoteVH(PaymentMethodsListNoteBinding.inflate(inflater, parent, false))
            else -> throw CheckoutException("Unexpected viewType on onCreateViewHolder - $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return paymentMethods[position].getViewType()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is HeaderVH -> holder.bind(paymentMethods[position] as PaymentMethodHeader, onPaymentMethodSelectedCallback)
            is StoredPaymentMethodVH -> holder.bind(
                paymentMethods[position] as StoredPaymentMethodModel,
                onStoredPaymentRemovedCallback,
                onPaymentMethodSelectedCallback
            )
            is PaymentMethodVH -> holder.bind(
                paymentMethods[position] as PaymentMethodModel,
                onPaymentMethodSelectedCallback
            )
            is GiftCardPaymentMethodVH -> holder.bind(paymentMethods[position] as GiftCardPaymentMethodModel)
            is NoteVH -> holder.bind(paymentMethods[position] as PaymentMethodNote)
        }
    }

    override fun getItemCount(): Int {
        return paymentMethods.size
    }

    class StoredPaymentMethodVH(
        private val binding: RemovablePaymentMethodsListItemBinding,
        private val imageLoader: ImageLoader,
        private val onUnderlayExpandListener: ((AdyenSwipeToRevealLayout) -> Unit)? = null
    ) : BaseViewHolder(binding) {

        fun bind(
            model: StoredPaymentMethodModel,
            onStoredPaymentRemovedCallback: OnStoredPaymentRemovedCallback?,
            onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback?
        ) {
            with(binding) {
                when (model) {
                    is StoredCardModel -> bindStoredCard(model, imageLoader)
                    is GenericStoredModel -> bindGenericStored(model)
                }
                paymentMethodItemUnderlayButton.setOnClickListener {
                    showRemoveStoredPaymentDialog(
                        model,
                        onStoredPaymentRemovedCallback
                    )
                }
                swipeToRevealLayout.apply {
                    setUnderlayListener { view ->
                        onUnderlayExpandListener?.invoke(view)
                    }
                    setOnMainClickListener {
                        onPaymentMethodSelectedCallback?.onStoredPaymentMethodSelected(model)
                    }
                    setDragLocked(!model.isRemovable)
                }
            }
        }

        private fun bindStoredCard(model: StoredCardModel, imageLoader: ImageLoader) {
            with(binding) {
                val context = root.context
                textViewText.text = context.getString(R.string.card_number_4digit, model.lastFour)
                imageLoader.load(model.imageId, imageViewLogo)
                textViewDetail.apply {
                    text = DateUtils.parseDateToView(model.expiryMonth, model.expiryYear)
                    isVisible = true
                }
                textViewEndText.isVisible = false
            }
        }

        private fun bindGenericStored(model: GenericStoredModel) {
            with(binding) {
                textViewText.text = model.name
                textViewDetail.isVisible = false
                imageLoader.load(model.imageId, imageViewLogo)
                textViewEndText.isVisible = false
            }
        }

        private fun showRemoveStoredPaymentDialog(
            model: StoredPaymentMethodModel,
            onStoredPaymentRemovedCallback: OnStoredPaymentRemovedCallback?
        ) {
            AlertDialog.Builder(binding.root.context)
                .setTitle(R.string.checkout_giftcard_remove_gift_cards_title)
                .setMessage(R.string.checkout_remove_stored_payment_method_body)
                .setPositiveButton(R.string.checkout_giftcard_remove_gift_cards_positive_button) { dialog, _ ->
                    onStoredPaymentRemovedCallback?.onStoredPaymentMethodRemoved(model)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.checkout_giftcard_remove_gift_cards_negative_button) { dialog, _ ->
                    (binding.root as? AdyenSwipeToRevealLayout)?.collapseUnderlay()
                    dialog.dismiss()
                }
                .show()
        }
    }

    class PaymentMethodVH(
        private val binding: PaymentMethodsListItemBinding,
        private val imageLoader: ImageLoader
    ) : BaseViewHolder(binding) {
        fun bind(
            model: PaymentMethodModel,
            onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback?
        ) = with(binding) {
            textViewText.text = model.name
            textViewDetail.isVisible = false

            imageViewLogo.borderEnabled = model.drawIconBorder
            imageLoader.load(model.icon, imageViewLogo)

            itemView.setOnClickListener {
                onPaymentMethodSelectedCallback?.onPaymentMethodSelected(model)
            }

            textViewEndText.isVisible = false
        }
    }

    class GiftCardPaymentMethodVH(
        private val binding: PaymentMethodsListItemBinding,
        private val imageLoader: ImageLoader
    ) : BaseViewHolder(binding) {
        fun bind(model: GiftCardPaymentMethodModel) = with(binding) {
            val context = binding.root.context
            textViewText.text = context.getString(R.string.card_number_4digit, model.lastFour)
            imageLoader.load(model.imageId, imageViewLogo)
            if (model.transactionLimit == null || model.shopperLocale == null) {
                textViewDetail.isVisible = false
            } else {
                val value = CurrencyUtils.formatAmount(
                    model.transactionLimit,
                    model.shopperLocale
                )
                textViewDetail.apply {
                    isVisible = true
                    text = context.getString(R.string.checkout_giftcard_max_transaction_limit, value)
                }
            }
            if (model.amount == null || model.shopperLocale == null) {
                textViewEndText.isVisible = false
            } else {
                val value = CurrencyUtils.formatAmount(
                    model.amount,
                    model.shopperLocale
                )
                textViewEndText.apply {
                    isVisible = true
                    text = context.getString(R.string.checkout_negative_amount, value)
                }
            }
            itemView.setOnClickListener(null)
        }
    }

    class HeaderVH(private val binding: PaymentMethodsListHeaderBinding) : BaseViewHolder(binding) {
        fun bind(
            model: PaymentMethodHeader,
            onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback?
        ) = with(binding) {
            paymentMethodHeaderTitle.setText(model.titleResId)
            if (model.actionResId == null) {
                paymentMethodHeaderAction.isVisible = false
            } else {
                paymentMethodHeaderAction.apply {
                    isVisible = true
                    setText(model.actionResId)
                    setOnClickListener {
                        onPaymentMethodSelectedCallback?.onHeaderActionSelected(model)
                    }
                }
            }
        }
    }

    class NoteVH(private val binding: PaymentMethodsListNoteBinding) : BaseViewHolder(binding) {
        fun bind(model: PaymentMethodNote) = with(binding) {
            paymentMethodNote.text = model.note
        }
    }

    open class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnPaymentMethodSelectedCallback {
        fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel)
        fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel)
        fun onHeaderActionSelected(header: PaymentMethodHeader)
    }

    interface OnStoredPaymentRemovedCallback {
        fun onStoredPaymentMethodRemoved(storedPaymentMethodModel: StoredPaymentMethodModel)
    }
}
