/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/5/2023.
 */

package com.adyen.checkout.action.core.internal

import com.adyen.checkout.adyen3ds2.old.Adyen3DS2Configuration
import com.adyen.checkout.await.old.AwaitConfiguration
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.old.RedirectConfiguration
import com.adyen.checkout.twint.action.TwintActionConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration

internal interface ActionHandlingConfigurationBuilder<BuilderT> {

    /**
     * Add configuration for 3DS2 action.
     */
    fun add3ds2ActionConfiguration(configuration: Adyen3DS2Configuration): BuilderT

    /**
     * Add configuration for Await action.
     */
    fun addAwaitActionConfiguration(configuration: AwaitConfiguration): BuilderT

    /**
     * Add configuration for QR code action.
     */
    fun addQRCodeActionConfiguration(configuration: QRCodeConfiguration): BuilderT

    /**
     * Add configuration for Redirect action.
     */
    fun addRedirectActionConfiguration(configuration: RedirectConfiguration): BuilderT

    /**
     * Add configuration for Twint action.
     */
    fun addTwintActionConfiguration(configuration: TwintActionConfiguration): BuilderT

    /**
     * Add configuration for Voucher action.
     */
    fun addVoucherActionConfiguration(configuration: VoucherConfiguration): BuilderT

    /**
     * Add configuration for WeChat Pay action.
     */
    fun addWeChatPayActionConfiguration(configuration: WeChatPayActionConfiguration): BuilderT
}
