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
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.util.DateUtils
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.dropin.R

@SuppressWarnings("ComplexMethod", "TooManyFunctions")
class PaymentMethodAdapter(
    private val paymentMethodsListModel: PaymentMethodsListModel,
    private val imageLoader: ImageLoader,
    private val onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback
) : RecyclerView.Adapter<PaymentMethodAdapter.BaseViewHolder>() {

    private val headerCount =
        if (paymentMethodsListModel.storedPaymentMethods.isEmpty()) 1
        else 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            PAYMENT_METHODS_HEADER -> HeaderVH(getView(parent, R.layout.payment_methods_list_header))
            STORED_PAYMENT_METHOD -> StoredPaymentMethodVH(getView(parent, R.layout.payment_methods_list_item))
            PAYMENT_METHOD -> PaymentMethodVH(getView(parent, R.layout.payment_methods_list_item))
            else -> throw CheckoutException("Unexpected viewType on onCreateViewHolder - $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> PAYMENT_METHODS_HEADER // Top is always a header
            noStored() -> PAYMENT_METHOD // only regular payment methods left
            // has stored payments
            position <= paymentMethodsListModel.storedPaymentMethods.size -> STORED_PAYMENT_METHOD // 0 is header then stored payments
            position == (paymentMethodsListModel.storedPaymentMethods.size + 1) -> PAYMENT_METHODS_HEADER // 2nd header
            else -> PAYMENT_METHOD
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is HeaderVH -> bindHeader(holder, position)
            is StoredPaymentMethodVH -> bindStoredPaymentMethod(holder, position)
            is PaymentMethodVH -> bindPaymentMethod(holder, position)
        }
    }

    private fun bindHeader(holder: HeaderVH, position: Int) {
        if (position == 0) {
            holder.title.setText(
                if (noStored()) R.string.payment_methods_header
                else R.string.store_payment_methods_header
            )
        } else {
            holder.title.setText(R.string.other_payment_methods)
        }
    }

    private fun bindStoredPaymentMethod(holder: StoredPaymentMethodVH, position: Int) {
        val storedPaymentMethod = getStoredPaymentMethodAt(position)

        when (storedPaymentMethod) {
            is StoredCardModel -> bindStoredCard(holder, storedPaymentMethod)
            is GenericStoredModel -> {
                holder.text.text = storedPaymentMethod.name
                holder.detail.visibility = View.GONE
                imageLoader.load(storedPaymentMethod.imageId, holder.logo)
            }
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

    private fun bindPaymentMethod(holder: PaymentMethodVH, position: Int) {
        val paymentMethod = getPaymentMethodAt(position)

        holder.text.text = paymentMethod.name
        holder.detail.visibility = View.GONE

        val txVariant = when (paymentMethod.type) {
            PaymentMethodTypes.SCHEME -> CARD_LOGO_TYPE
            PaymentMethodTypes.GOOGLE_PAY -> GOOGLE_PAY_LOGO_TYPE
            else -> paymentMethod.type
        }

        imageLoader.load(txVariant, holder.logo)

        holder.itemView.setOnClickListener {
            onPaymentMethodClick(paymentMethod)
        }
    }

    override fun getItemCount(): Int {
        return headerCount + paymentMethodsListModel.storedPaymentMethods.size + paymentMethodsListModel.paymentMethods.size
    }

    private fun getStoredPaymentMethodAt(position: Int): StoredPaymentMethodModel {
        return paymentMethodsListModel.storedPaymentMethods[position - 1]
    }

    private fun getPaymentMethodAt(position: Int): PaymentMethodModel {
        return if (noStored())
            paymentMethodsListModel.paymentMethods[position - headerCount]
        else
            paymentMethodsListModel.paymentMethods[position - (paymentMethodsListModel.storedPaymentMethods.size + headerCount)]
    }

    private fun noStored(): Boolean {
        return paymentMethodsListModel.storedPaymentMethods.isEmpty()
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

        // View types
        internal const val PAYMENT_METHODS_HEADER = 1
        internal const val STORED_PAYMENT_METHOD = 2
        internal const val PAYMENT_METHOD = 3

        private const val CARD_LOGO_TYPE = "card"
        private const val GOOGLE_PAY_LOGO_TYPE = "googlepay"
    }

    interface OnPaymentMethodSelectedCallback {
        fun onStoredPaymentMethodSelected(storedPaymentMethodModel: StoredPaymentMethodModel)
        fun onPaymentMethodSelected(paymentMethod: PaymentMethodModel)
    }

    class StoredPaymentMethodVH(rootView: View) : BaseViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val detail: TextView = rootView.findViewById(R.id.textView_detail)
        internal val logo: ImageView = rootView.findViewById(R.id.imageView_logo)
    }

    class PaymentMethodVH(rootView: View) : BaseViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val detail: TextView = rootView.findViewById(R.id.textView_detail)
        internal val logo: ImageView = rootView.findViewById(R.id.imageView_logo)
    }

    class HeaderVH(rootView: View) : BaseViewHolder(rootView) {
        internal val title: TextView = rootView.findViewById(R.id.payment_method_header)
    }

    open class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)
}
