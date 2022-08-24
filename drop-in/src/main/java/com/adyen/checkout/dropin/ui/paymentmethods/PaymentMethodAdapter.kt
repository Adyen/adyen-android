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
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.view.AdyenSwipeToRevealLayout
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.adyen.checkout.components.util.CurrencyUtils
import com.adyen.checkout.components.util.DateUtils
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.GIFT_CARD_PAYMENT_METHOD
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.PAYMENT_METHOD
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.PAYMENT_METHODS_HEADER
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.PAYMENT_METHODS_NOTE
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.STORED_PAYMENT_METHOD

@SuppressWarnings("TooManyFunctions")
class PaymentMethodAdapter @JvmOverloads constructor(
    private val paymentMethods: MutableList<PaymentMethodListItem>,
    private val imageLoader: ImageLoader,
    private val onUnderlayExpandListener: ((AdyenSwipeToRevealLayout) -> Unit)? = null
) : RecyclerView.Adapter<PaymentMethodAdapter.BaseViewHolder>() {

    constructor(
        paymentMethods: Collection<PaymentMethodListItem>,
        imageLoader: ImageLoader,
        onUnderlayExpandListener: ((AdyenSwipeToRevealLayout) -> Unit)? = null
    ) : this(paymentMethods.toMutableList(), imageLoader, onUnderlayExpandListener)

    private var onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback? = null
    private var onStoredPaymentRemovedCallback: OnStoredPaymentRemovedCallback? = null

    fun setPaymentMethodSelectedCallback(onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback) {
        this.onPaymentMethodSelectedCallback = onPaymentMethodSelectedCallback
    }

    fun setStoredPaymentRemovedCallback(onStoredPaymentRemovedCallback: OnStoredPaymentRemovedCallback) {
        this.onStoredPaymentRemovedCallback = onStoredPaymentRemovedCallback
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePaymentMethods(paymentMethods: List<PaymentMethodListItem>) {
        this.paymentMethods.clear()
        this.paymentMethods.addAll(paymentMethods)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            PAYMENT_METHODS_HEADER -> HeaderVH(getView(parent, R.layout.payment_methods_list_header))
            STORED_PAYMENT_METHOD -> StoredPaymentMethodVH(
                getView(parent, R.layout.removable_payment_methods_list_item)
            )
            PAYMENT_METHOD -> PaymentMethodVH(getView(parent, R.layout.payment_methods_list_item))
            GIFT_CARD_PAYMENT_METHOD -> GiftCardPaymentMethodVH(getView(parent, R.layout.payment_methods_list_item))
            PAYMENT_METHODS_NOTE -> NoteVH(getView(parent, R.layout.payment_methods_list_note))
            else -> throw CheckoutException("Unexpected viewType on onCreateViewHolder - $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return paymentMethods[position].getViewType()
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is HeaderVH -> bindHeader(holder, position)
            is StoredPaymentMethodVH -> bindStoredPaymentMethod(holder, position)
            is PaymentMethodVH -> bindPaymentMethod(holder, position)
            is GiftCardPaymentMethodVH -> bindGiftCardPaymentMethod(holder, position)
            is NoteVH -> bindNote(holder, position)
        }
    }

    private fun bindHeader(holder: HeaderVH, position: Int) {
        val header = getHeaderAt(position)
        holder.title.setText(header.titleResId)
        with(holder.action) {
            if (header.actionResId == null) {
                isVisible = false
            } else {
                isVisible = true
                setText(header.actionResId)
                setOnClickListener {
                    onHeaderActionClick(header)
                }
            }
        }
    }

    private fun bindStoredPaymentMethod(holder: StoredPaymentMethodVH, position: Int) {
        val storedPaymentMethod = getStoredPaymentMethodAt(position)

        when (storedPaymentMethod) {
            is StoredCardModel -> bindStoredCard(holder, storedPaymentMethod)
            is GenericStoredModel -> bindGenericStored(holder, storedPaymentMethod)
        }

        holder.underlayButton.setOnClickListener {
            showRemoveStoredPaymentDialog(holder.itemView, storedPaymentMethod)
        }

        holder.swipeToRevealLayout.apply {
            setUnderlayListener { view ->
                onUnderlayExpandListener?.invoke(view)
            }
            setOnMainClickListener {
                onStoredPaymentMethodClick(storedPaymentMethod)
            }
            setDragLocked(!storedPaymentMethod.isRemovable)
        }
    }

    private fun showRemoveStoredPaymentDialog(itemView: View, storedPaymentMethodModel: StoredPaymentMethodModel) {
        AlertDialog.Builder(itemView.context)
            .setTitle(R.string.checkout_giftcard_remove_gift_cards_title)
            .setMessage(R.string.checkout_remove_stored_payment_method_body)
            .setPositiveButton(R.string.checkout_giftcard_remove_gift_cards_positive_button) { dialog, _ ->
                onStoredPaymentRemovedCallback?.onStoredPaymentMethodRemoved(storedPaymentMethodModel)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.checkout_giftcard_remove_gift_cards_negative_button) { dialog, _ ->
                (itemView as? AdyenSwipeToRevealLayout)?.collapseUnderlay()
                dialog.dismiss()
            }
            .show()
    }

    private fun bindStoredCard(holder: StoredPaymentMethodVH, storedCardModel: StoredCardModel) {
        val context = holder.itemView.context
        holder.text.text = context.getString(R.string.card_number_4digit, storedCardModel.lastFour)
        imageLoader.load(storedCardModel.imageId, holder.logo)
        holder.detail.text = DateUtils.parseDateToView(storedCardModel.expiryMonth, storedCardModel.expiryYear)
        holder.detail.isVisible = true
        holder.endText.isVisible = false
    }

    private fun bindGenericStored(holder: StoredPaymentMethodVH, genericStoredModel: GenericStoredModel) {
        holder.text.text = genericStoredModel.name
        holder.detail.isVisible = false
        imageLoader.load(genericStoredModel.imageId, holder.logo)
        holder.endText.isVisible = false
    }

    private fun bindPaymentMethod(holder: PaymentMethodVH, position: Int) {
        val paymentMethod = getPaymentMethodAt(position)

        holder.text.text = paymentMethod.name
        holder.detail.isVisible = false

        holder.logo.borderEnabled = paymentMethod.drawIconBorder
        imageLoader.load(paymentMethod.icon, holder.logo)

        holder.itemView.setOnClickListener {
            onPaymentMethodClick(paymentMethod)
        }
        holder.endText.isVisible = false
    }

    private fun bindGiftCardPaymentMethod(holder: GiftCardPaymentMethodVH, position: Int) {
        val giftCardPaymentMethod = getGiftCardPaymentMethodAt(position)

        val context = holder.itemView.context
        holder.text.text = context.getString(R.string.card_number_4digit, giftCardPaymentMethod.lastFour)
        imageLoader.load(giftCardPaymentMethod.imageId, holder.logo)
        if (giftCardPaymentMethod.transactionLimit == null || giftCardPaymentMethod.shopperLocale == null) {
            holder.detail.isVisible = false
        } else {
            holder.detail.isVisible = true
            val value =
                CurrencyUtils.formatAmount(giftCardPaymentMethod.transactionLimit, giftCardPaymentMethod.shopperLocale)
            holder.detail.text = context.getString(R.string.checkout_giftcard_max_transaction_limit, value)
        }
        if (giftCardPaymentMethod.amount == null || giftCardPaymentMethod.shopperLocale == null) {
            holder.endText.isVisible = false
        } else {
            holder.endText.isVisible = true
            val value = CurrencyUtils.formatAmount(giftCardPaymentMethod.amount, giftCardPaymentMethod.shopperLocale)
            holder.endText.text = context.getString(R.string.checkout_negative_amount, value)
        }

        holder.itemView.setOnClickListener(null)
    }

    private fun bindNote(holder: NoteVH, position: Int) {
        val header = getNoteAt(position)
        holder.note.text = header.note
    }

    override fun getItemCount(): Int {
        return paymentMethods.size
    }

    private fun getHeaderAt(position: Int): PaymentMethodHeader {
        return paymentMethods[position] as PaymentMethodHeader
    }

    private fun getStoredPaymentMethodAt(position: Int): StoredPaymentMethodModel {
        return paymentMethods[position] as StoredPaymentMethodModel
    }

    private fun getPaymentMethodAt(position: Int): PaymentMethodModel {
        return paymentMethods[position] as PaymentMethodModel
    }

    private fun getGiftCardPaymentMethodAt(position: Int): GiftCardPaymentMethodModel {
        return paymentMethods[position] as GiftCardPaymentMethodModel
    }

    private fun getNoteAt(position: Int): PaymentMethodNote {
        return paymentMethods[position] as PaymentMethodNote
    }

    private fun onStoredPaymentMethodClick(storedPaymentMethodModel: StoredPaymentMethodModel) {
        onPaymentMethodSelectedCallback?.onStoredPaymentMethodSelected(storedPaymentMethodModel)
    }

    private fun onPaymentMethodClick(paymentMethod: PaymentMethodModel) {
        onPaymentMethodSelectedCallback?.onPaymentMethodSelected(paymentMethod)
    }

    private fun onHeaderActionClick(header: PaymentMethodHeader) {
        onPaymentMethodSelectedCallback?.onHeaderActionSelected(header)
    }

    private fun getView(parent: ViewGroup, id: Int): View {
        return LayoutInflater.from(parent.context).inflate(id, parent, false)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }

    interface OnPaymentMethodSelectedCallback {
        fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel)
        fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel)
        fun onHeaderActionSelected(header: PaymentMethodHeader)
    }

    interface OnStoredPaymentRemovedCallback {
        fun onStoredPaymentMethodRemoved(storedPaymentMethodModel: StoredPaymentMethodModel)
    }

    class StoredPaymentMethodVH(rootView: View) : BaseViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val detail: TextView = rootView.findViewById(R.id.textView_detail)
        internal val logo: RoundCornerImageView = rootView.findViewById(R.id.imageView_logo)
        internal val endText: TextView = rootView.findViewById(R.id.textView_endText)
        internal val swipeToRevealLayout: AdyenSwipeToRevealLayout = rootView.findViewById(R.id.swipeToRevealLayout)
        internal val underlayButton: FrameLayout = rootView.findViewById(R.id.payment_method_item_underlay_button)
    }

    class PaymentMethodVH(rootView: View) : BaseViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val detail: TextView = rootView.findViewById(R.id.textView_detail)
        internal val logo: RoundCornerImageView = rootView.findViewById(R.id.imageView_logo)
        internal val endText: TextView = rootView.findViewById(R.id.textView_endText)
    }

    class GiftCardPaymentMethodVH(rootView: View) : BaseViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val detail: TextView = rootView.findViewById(R.id.textView_detail)
        internal val logo: RoundCornerImageView = rootView.findViewById(R.id.imageView_logo)
        internal val endText: TextView = rootView.findViewById(R.id.textView_endText)
    }

    class HeaderVH(rootView: View) : BaseViewHolder(rootView) {
        internal val title: TextView = rootView.findViewById(R.id.payment_method_header_title)
        internal val action: TextView = rootView.findViewById(R.id.payment_method_header_action)
    }

    class NoteVH(rootView: View) : BaseViewHolder(rootView) {
        internal val note: TextView = rootView.findViewById(R.id.payment_method_note)
    }

    open class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)
}
