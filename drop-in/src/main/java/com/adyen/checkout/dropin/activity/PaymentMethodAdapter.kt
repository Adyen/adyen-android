/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/3/2019.
 */

package com.adyen.checkout.dropin.activity

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.adyen.checkout.base.ui.R
import com.adyen.checkout.core.log.LogUtil

class PaymentMethodAdapter(
    private var paymentMethods: List<PaymentMethodModel>,
    private val onPaymentMethodSelectedCallback: OnPaymentMethodSelectedCallback
) : RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder>() {

    companion object {
        internal val TAG = LogUtil.getTag()
    }

    class PaymentMethodViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {
        internal val text: TextView = rootView.findViewById(R.id.textView_text)
        internal val logo: ImageView = rootView.findViewById(R.id.imageView_logo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_list_with_image, parent, false)
        return PaymentMethodViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return paymentMethods.size
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        val paymentMethodModel = paymentMethods[position]

        holder.text.text = paymentMethodModel.paymentMethod.name
        holder.logo.setImageDrawable(paymentMethodModel.logo)

        holder.rootView.setOnClickListener {
            onPaymentMethodSelectedCallback.onPaymentMethodSelected(paymentMethods[position])
        }
    }

    interface OnPaymentMethodSelectedCallback {
        fun onPaymentMethodSelected(paymentMethodModel: PaymentMethodModel)
    }

    fun updatePaymentMethodsList(paymentMethodsParam: List<PaymentMethodModel>) {
        val sizeChanged = paymentMethodsParam.size != paymentMethods.size
        paymentMethods = paymentMethodsParam
        if (sizeChanged) {
            notifyDataSetChanged()
        } else {
            for (position in 0 until paymentMethods.size) {
                if (paymentMethods[position].isUpdated) {
                    notifyItemChanged(position)
                    paymentMethods[position].consumeUpdate()
                }
            }
        }
    }
}
