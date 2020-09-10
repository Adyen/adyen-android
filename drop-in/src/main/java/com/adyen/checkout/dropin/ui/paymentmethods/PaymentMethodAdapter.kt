/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.adyen.checkout.base.api.ImageLoader
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.util.DateUtils
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.dropin.R

@SuppressWarnings("ComplexMethod", "TooManyFunctions")
class PaymentMethodAdapter(
    private var paymentMethodsModel: PaymentMethodsModel,
    private var imageLoader: ImageLoader,
    private var showInExpandStatus: Boolean,
    private val onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback
) : androidx.recyclerview.widget.RecyclerView.Adapter<PaymentMethodAdapter.BaseViewHolder>() {

    private var expandListOfItem = emptyList<PaymentMethod>()
    private var collapseListOfItem = emptyList<PaymentMethod>()

    init {
        initAdapter()
    }

    companion object {
        internal val TAG = LogUtil.getTag()

        internal const val PAYMENT_METHODS_HEADER = 1
        internal const val PAYMENT_METHODS_FOOTER = 2
        internal const val PAYMENT_METHOD_ITEMS = 3

        internal val HEADER_PLACEHOLDER_ITEM_SP = PaymentMethod()
        internal val HEADER_PLACEHOLDER_ITEM_P = PaymentMethod()
        internal val FOOTER_PLACEHOLDER_ITEM = PaymentMethod()

        private const val CARD_LOGO_TYPE = "card"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            PAYMENT_METHODS_HEADER -> PaymentMethodsHeaderVH(getView(parent, R.layout.payment_methods_list_header))
            PAYMENT_METHODS_FOOTER -> PaymentMethodsFooterVH(getView(parent, R.layout.payment_methods_list_footer))
            else -> PaymentMethodVH(getView(parent, R.layout.payment_methods_list_item))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getPaymentMethod(position)) {
            HEADER_PLACEHOLDER_ITEM_P, HEADER_PLACEHOLDER_ITEM_SP -> PAYMENT_METHODS_HEADER
            FOOTER_PLACEHOLDER_ITEM -> PAYMENT_METHODS_FOOTER
            else -> PAYMENT_METHOD_ITEMS
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val paymentMethod = getPaymentMethod(position)
        val context: Context = holder.itemView.context

        when {
            (holder is PaymentMethodVH) -> {
                if (paymentMethod is StoredPaymentMethod) {
                    holder.text.text = context.getString(R.string.card_number_4digit, paymentMethod.lastFour)
                    holder.detail.text = context.getString(R.string.expires_in, paymentMethod.expiryMonth,
                            DateUtils.removeFirstTwoDigitFromYear(paymentMethod.expiryYear))
                    holder.detail.visibility = View.VISIBLE
                } else {
                    holder.text.text = paymentMethod?.name
                    holder.detail.visibility = View.GONE
                }

                var txVariant = when (paymentMethod.type) {
                    PaymentMethodTypes.SCHEME -> if (paymentMethod is StoredPaymentMethod) paymentMethod.brand else CARD_LOGO_TYPE
                    else -> paymentMethod.type!!
                }

                imageLoader.load(txVariant, holder.logo)

                holder.itemView.setOnClickListener {
                    onItemClick(getPaymentMethod(position))
                }
            }

            (holder is PaymentMethodsHeaderVH) -> {
                when (paymentMethod) {
                    HEADER_PLACEHOLDER_ITEM_SP -> holder.title.setText(R.string.store_payment_methods_header)
                    HEADER_PLACEHOLDER_ITEM_P -> {
                        holder.title.setText(if (!showInExpandStatus) R.string.other_payment_methods
                        else R.string.payment_methods_header)
                    }
                }
            }

            (holder is PaymentMethodsFooterVH) -> {
                holder.pay.setOnClickListener {
                    onItemClick(paymentMethodsModel.storedPaymentMethods.first())
                }

                holder.others.setOnClickListener {
                    showInExpandStatus = true
                    it.visibility = View.GONE
                    notifyItemRangeInserted(collapseListOfItem.size, expandListOfItem.size)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return getList().size
    }

    private fun onItemClick(paymentMethod: PaymentMethod) {
        onPaymentMethodSelectedCallback.onPaymentMethodSelected(paymentMethod, showInExpandStatus)
    }

    private fun getList(): List<PaymentMethod> {
        if (showInExpandStatus) {
            return expandListOfItem
        }

        return collapseListOfItem
    }

    private fun getPaymentMethod(position: Int): PaymentMethod {
        return getList()[position]
    }

    private fun getStorePaymentMethodsList(): List<PaymentMethod> {
        if (paymentMethodsModel.storedPaymentMethods.size > 0) {
            return listOf(HEADER_PLACEHOLDER_ITEM_SP) + paymentMethodsModel.storedPaymentMethods
        }

        return emptyList()
    }

    private fun getFirstStorePaymentMethodsList(): List<PaymentMethod> {
        if (paymentMethodsModel.storedPaymentMethods.size > 0) {
            return listOf(HEADER_PLACEHOLDER_ITEM_SP) + paymentMethodsModel.storedPaymentMethods.first()
        }

        return emptyList()
    }

    private fun getPaymentMethodsList(): List<PaymentMethod> {
        if (paymentMethodsModel.paymentMethods.size > 0) {
            return listOf(HEADER_PLACEHOLDER_ITEM_P) + paymentMethodsModel.paymentMethods
        }

        return emptyList()
    }

    private fun getView(parent: ViewGroup, id: Int): View {
        return LayoutInflater.from(parent.context).inflate(id, parent, false)
    }

    private fun initAdapter() {
        expandListOfItem = getStorePaymentMethodsList() + getPaymentMethodsList()
        collapseListOfItem = getFirstStorePaymentMethodsList() + listOf(FOOTER_PLACEHOLDER_ITEM)

        if (paymentMethodsModel.storedPaymentMethods.size == 0 && !showInExpandStatus) {
            showInExpandStatus = true
        }
    }

    fun updatePaymentMethodsList(paymentMethodsModel: PaymentMethodsModel) {
        this.paymentMethodsModel = paymentMethodsModel
        initAdapter()
    }

    interface OnPaymentMethodSelectedCallback {
        fun onPaymentMethodSelected(paymentMethod: PaymentMethod, isInExpandMode: Boolean)
    }

    class PaymentMethodVH(rootView: View) : BaseViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val detail: TextView = rootView.findViewById(R.id.textView_detail)
        internal val logo: ImageView = rootView.findViewById(R.id.imageView_logo)
    }

    class PaymentMethodsHeaderVH(rootView: View) : BaseViewHolder(rootView) {
        internal val title: TextView = rootView.findViewById(R.id.payment_method_header)
    }

    class PaymentMethodsFooterVH(rootView: View) : BaseViewHolder(rootView) {
        internal val others: TextView = rootView.findViewById(R.id.others)
        internal val pay: Button = rootView.findViewById(R.id.payButton)
    }

    open class BaseViewHolder(rootView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(rootView)
}
