package com.adyen.checkout.action

import androidx.annotation.RestrictTo
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionHandlingConfigurationBuilder {

    /**
     * Add configuration for 3DS2 action.
     */
    fun add3ds2ActionConfiguration(configuration: Adyen3DS2Configuration): ActionHandlingConfigurationBuilder

    /**
     * Add configuration for Await action.
     */
    fun addAwaitActionConfiguration(configuration: AwaitConfiguration): ActionHandlingConfigurationBuilder

    /**
     * Add configuration for QR code action.
     */
    fun addQRCodeActionConfiguration(configuration: QRCodeConfiguration): ActionHandlingConfigurationBuilder

    /**
     * Add configuration for Redirect action.
     */
    fun addRedirectActionConfiguration(configuration: RedirectConfiguration): ActionHandlingConfigurationBuilder

    /**
     * Add configuration for Voucher action.
     */
    fun addVoucherActionConfiguration(configuration: VoucherConfiguration): ActionHandlingConfigurationBuilder

    /**
     * Add configuration for WeChat Pay action.
     */
    fun addWeChatPayActionConfiguration(configuration: WeChatPayActionConfiguration): ActionHandlingConfigurationBuilder
}
