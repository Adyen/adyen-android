/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.adyen.checkout.components.util.DateUtils
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.PAYMENT_METHOD
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.PAYMENT_METHODS_HEADER
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodListItem.Companion.STORED_PAYMENT_METHOD

@SuppressWarnings("TooManyFunctions")
class PaymentMethodAdapter(
    private val paymentMethods: List<PaymentMethodListItem>,
    private val imageLoader: ImageLoader,
    private val onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback
) : RecyclerView.Adapter<PaymentMethodAdapter.BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            PAYMENT_METHODS_HEADER -> HeaderVH(getView(parent, R.layout.payment_methods_list_header))
            STORED_PAYMENT_METHOD -> StoredPaymentMethodVH(getView(parent, R.layout.payment_methods_list_item))
            PAYMENT_METHOD -> PaymentMethodVH(getView(parent, R.layout.payment_methods_list_item))
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
        }
    }

    private fun bindHeader(holder: HeaderVH, position: Int) {
        val header = getHeaderAt(position)
        holder.title.setText(header.titleResId)
    }

    private fun bindStoredPaymentMethod(holder: StoredPaymentMethodVH, position: Int) {
        val storedPaymentMethod = getStoredPaymentMethodAt(position)

        when (storedPaymentMethod) {
            is StoredCardModel -> bindStoredCard(holder, storedPaymentMethod)
            is GenericStoredModel -> bindGenericStored(holder, storedPaymentMethod)
        }

        holder.itemView.setOnClickListener {
            onStoredPaymentMethodClick(storedPaymentMethod)
        }
    }

    private fun bindStoredCard(holder: StoredPaymentMethodVH, storedCardModel: StoredCardModel) {
        val context = holder.itemView.context
        holder.text.text = context.getString(R.string.card_number_4digit, storedCardModel.lastFour)
        imageLoader.load(storedCardModel.imageId, holder.logo)
        holder.detail.text = DateUtils.parseDateToView(storedCardModel.expiryMonth, storedCardModel.expiryYear)
        holder.detail.visibility = View.VISIBLE
    }

    private fun bindGenericStored(holder: StoredPaymentMethodVH, genericStoredModel: GenericStoredModel) {
        holder.text.text = genericStoredModel.name
        holder.detail.visibility = View.GONE
        imageLoader.load(genericStoredModel.imageId, holder.logo)
    }

    private fun bindPaymentMethod(holder: PaymentMethodVH, position: Int) {
        val paymentMethod = getPaymentMethodAt(position)

        holder.text.text = paymentMethod.name
        holder.detail.visibility = View.GONE

        holder.logo.setRoundingDisabled(paymentMethod.isBorderDisabled)
        imageLoader.load(paymentMethod.icon, holder.logo)

        holder.itemView.setOnClickListener {
            onPaymentMethodClick(paymentMethod)
        }
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

    private fun onStoredPaymentMethodClick(storedPaymentMethodModel: StoredPaymentMethodModel) {
        onPaymentMethodSelectedCallback.onStoredPaymentMethodSelected(storedPaymentMethodModel)
    }

    private fun onPaymentMethodClick(paymentMethod: PaymentMethodModel) {
        onPaymentMethodSelectedCallback.onPaymentMethodSelected(paymentMethod)
    }

    private fun getView(parent: ViewGroup, id: Int): View {
        return LayoutInflater.from(parent.context).inflate(id, parent, false)
    }

    companion object {
        internal val TAG = LogUtil.getTag()
    }

    interface OnPaymentMethodSelectedCallback {
        fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel)
        fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel)
    }

    class StoredPaymentMethodVH(rootView: View) : BaseViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val detail: TextView = rootView.findViewById(R.id.textView_detail)
        internal val logo: RoundCornerImageView = rootView.findViewById(R.id.imageView_logo)
    }

    class PaymentMethodVH(rootView: View) : BaseViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val detail: TextView = rootView.findViewById(R.id.textView_detail)
        internal val logo: RoundCornerImageView = rootView.findViewById(R.id.imageView_logo)
    }

    class HeaderVH(rootView: View) : BaseViewHolder(rootView) {
        internal val title: TextView = rootView.findViewById(R.id.payment_method_header)
    }

    open class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)
}
