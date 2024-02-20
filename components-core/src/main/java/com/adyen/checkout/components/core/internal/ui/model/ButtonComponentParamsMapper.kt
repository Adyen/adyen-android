package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.Configuration
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ButtonComponentParamsMapper(
    private val commonComponentParamsMapper: CommonComponentParamsMapper,
) {

    fun mapToParams(
        checkoutConfiguration: CheckoutConfiguration,
        deviceLocale: Locale,
        dropInOverrideParams: DropInOverrideParams?,
        componentSessionParams: SessionParams?,
        componentConfiguration: Configuration?
    ): ButtonComponentParams {
        val commonComponentParamsMapperData = commonComponentParamsMapper.mapToParams(
            checkoutConfiguration,
            deviceLocale,
            dropInOverrideParams,
            componentSessionParams,
        )
        return ButtonComponentParams(
            commonComponentParams = commonComponentParamsMapperData.commonComponentParams,
            isSubmitButtonVisible = (componentConfiguration as? ButtonConfiguration)?.isSubmitButtonVisible ?: true,
        )
    }
}
