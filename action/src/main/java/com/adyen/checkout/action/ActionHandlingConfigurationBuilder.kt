package com.adyen.checkout.action

import androidx.annotation.RestrictTo
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionHandlingConfigurationBuilder<BuilderT> {

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
     * Add configuration for Voucher action.
     */
    fun addVoucherActionConfiguration(configuration: VoucherConfiguration): BuilderT

    /**
     * Add configuration for WeChat Pay action.
     */
    fun addWeChatPayActionConfiguration(configuration: WeChatPayActionConfiguration): BuilderT
}
