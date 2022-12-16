/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.INVALID_TYPE
import com.adyen.checkout.components.image.loadLogo
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
internal class PaymentMethodAdapter @JvmOverloads constructor(
    private val onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback? = null,
    private val onStoredPaymentRemovedCallback: OnStoredPaymentRemovedCallback? = null,
    private val onUnderlayExpandListener: ((AdyenSwipeToRevealLayout) -> Unit)? = null
) : ListAdapter<PaymentMethodListItem, RecyclerView.ViewHolder>(PaymentMethodDiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return currentList.getOrNull(position)?.getViewType() ?: INVALID_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            PAYMENT_METHODS_HEADER -> HeaderVH(
                PaymentMethodsListHeaderBinding.inflate(inflater, parent, false)
            )
            STORED_PAYMENT_METHOD -> StoredPaymentMethodVH(
                RemovablePaymentMethodsListItemBinding.inflate(inflater, parent, false),
                onUnderlayExpandListener
            )
            PAYMENT_METHOD -> PaymentMethodVH(
                PaymentMethodsListItemBinding.inflate(inflater, parent, false),
            )
            GIFT_CARD_PAYMENT_METHOD -> GiftCardPaymentMethodVH(
                PaymentMethodsListItemBinding.inflate(inflater, parent, false),
            )
            PAYMENT_METHODS_NOTE -> NoteVH(PaymentMethodsListNoteBinding.inflate(inflater, parent, false))
            else -> throw CheckoutException("Unexpected viewType on onCreateViewHolder - $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList.getOrNull(position)
        when (holder) {
            is HeaderVH -> holder.bind(item as PaymentMethodHeader, onPaymentMethodSelectedCallback)
            is StoredPaymentMethodVH -> holder.bind(
                item as StoredPaymentMethodModel,
                onStoredPaymentRemovedCallback,
                onPaymentMethodSelectedCallback
            )
            is PaymentMethodVH -> holder.bind(
                item as PaymentMethodModel,
                onPaymentMethodSelectedCallback
            )
            is GiftCardPaymentMethodVH -> holder.bind(item as GiftCardPaymentMethodModel)
            is NoteVH -> holder.bind(item as PaymentMethodNote)
        }
    }

    override fun getItemCount(): Int = currentList.size

    private class StoredPaymentMethodVH(
        private val binding: RemovablePaymentMethodsListItemBinding,
        private val onUnderlayExpandListener: ((AdyenSwipeToRevealLayout) -> Unit)? = null
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            model: StoredPaymentMethodModel,
            onStoredPaymentRemovedCallback: OnStoredPaymentRemovedCallback?,
            onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback?
        ) {
            with(binding) {
                when (model) {
                    is StoredCardModel -> bindStoredCard(model)
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

        private fun bindStoredCard(model: StoredCardModel) {
            with(binding) {
                val context = root.context
                textViewTitle.text = context.getString(R.string.card_number_4digit, model.lastFour)
                imageViewLogo.loadLogo(
                    environment = model.environment,
                    txVariant = model.imageId,
                )
                textViewDetail.apply {
                    text = DateUtils.parseDateToView(model.expiryMonth, model.expiryYear)
                    isVisible = true
                }
                textViewAmount.isVisible = false
            }
        }

        private fun bindGenericStored(model: GenericStoredModel) {
            with(binding) {
                textViewTitle.text = model.name
                textViewDetail.isVisible = false
                imageViewLogo.loadLogo(
                    environment = model.environment,
                    txVariant = model.imageId,
                )
                textViewAmount.isVisible = false
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

    private class PaymentMethodVH(
        private val binding: PaymentMethodsListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            model: PaymentMethodModel,
            onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback?
        ) = with(binding) {
            textViewTitle.text = model.name
            textViewDetail.isVisible = false

            imageViewLogo.borderEnabled = model.drawIconBorder
            imageViewLogo.loadLogo(
                environment = model.environment,
                txVariant = model.icon,
            )

            itemView.setOnClickListener {
                onPaymentMethodSelectedCallback?.onPaymentMethodSelected(model)
            }

            textViewAmount.isVisible = false
        }
    }

    private class GiftCardPaymentMethodVH(
        private val binding: PaymentMethodsListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: GiftCardPaymentMethodModel) = with(binding) {
            val context = binding.root.context
            textViewTitle.text = context.getString(R.string.card_number_4digit, model.lastFour)
            imageViewLogo.loadLogo(
                environment = model.environment,
                txVariant = model.imageId,
            )
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
                textViewAmount.isVisible = false
            } else {
                val value = CurrencyUtils.formatAmount(
                    model.amount,
                    model.shopperLocale
                )
                textViewAmount.apply {
                    isVisible = true
                    text = context.getString(R.string.checkout_negative_amount, value)
                }
            }
            itemView.setOnClickListener(null)
        }
    }

    private class HeaderVH(private val binding: PaymentMethodsListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
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

    private class NoteVH(private val binding: PaymentMethodsListNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: PaymentMethodNote) = with(binding) {
            paymentMethodNote.text = model.note
        }
    }

    interface OnPaymentMethodSelectedCallback {
        fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel)
        fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel)
        fun onHeaderActionSelected(header: PaymentMethodHeader)
    }

    interface OnStoredPaymentRemovedCallback {
        fun onStoredPaymentMethodRemoved(storedPaymentMethodModel: StoredPaymentMethodModel)
    }

    object PaymentMethodDiffCallback : DiffUtil.ItemCallback<PaymentMethodListItem>() {
        override fun areItemsTheSame(oldItem: PaymentMethodListItem, newItem: PaymentMethodListItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: PaymentMethodListItem, newItem: PaymentMethodListItem): Boolean =
            areItemsTheSame(oldItem, newItem)
    }
}
