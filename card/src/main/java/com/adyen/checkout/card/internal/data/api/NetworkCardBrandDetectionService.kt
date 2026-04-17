/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/4/2026.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.BinLookupRequest
import com.adyen.checkout.card.internal.data.model.BinLookupResponse
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import java.util.UUID

internal class NetworkCardBrandDetectionService(
    private val cardEncryptor: BaseCardEncryptor,
    private val binLookupService: BinLookupService,
    private val publicKey: String?,
    private val supportedCardBrands: List<CardBrand>,
    private val paymentMethodType: String?,
) {

    suspend fun getCardBrands(bin: String): Result<List<DetectedCardType>> {
        adyenLog(AdyenLogLevel.DEBUG) { "fetching card brands from network - bin lookup" }
        val publicKey = publicKey
        if (publicKey == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Public key is null" }
            // no need for an error callback since BIN lookup failing is not a fatal error
            return Result.failure(GenericError("Public key is missing."))
        }
        val binLookupResult = runSuspendCatching {
            val encryptedBin = cardEncryptor.encryptBin(bin, publicKey)
            val stringCardBrands = supportedCardBrands.map { it.txVariant }
            val requestId = UUID.randomUUID().toString()
            val request = BinLookupRequest(encryptedBin, requestId, stringCardBrands, paymentMethodType)

            binLookupService.makeBinLookup(request)
        }
            .onFailure { e ->
                adyenLog(AdyenLogLevel.ERROR, e) { "getCardBrands - Error calling bin lookup" }
            }

        return binLookupResult.map(::mapResponse)
    }

    private fun mapResponse(binLookupResponse: BinLookupResponse): List<DetectedCardType> {
        adyenLog(AdyenLogLevel.VERBOSE) { "Brands: ${binLookupResponse.brands}" }

        // Any null or unmapped values are ignored, a null response becomes an empty list
        return binLookupResponse.brands.orEmpty().mapNotNull { brandResponse ->
            if (brandResponse.brand == null) return@mapNotNull null
            val cardBrand = CardBrand(txVariant = brandResponse.brand)
            DetectedCardType(
                cardBrand = cardBrand,
                isReliable = true,
                enableLuhnCheck = brandResponse.enableLuhnCheck == true,
                cvcPolicy = Brand.FieldPolicy.parse(
                    brandResponse.cvcPolicy ?: Brand.FieldPolicy.REQUIRED.value,
                ),
                expiryDatePolicy = Brand.FieldPolicy.parse(
                    brandResponse.expiryDatePolicy ?: Brand.FieldPolicy.REQUIRED.value,
                ),
                isSupported = brandResponse.supported != false,
                panLength = brandResponse.panLength,
                paymentMethodVariant = brandResponse.paymentMethodVariant,
                localizedBrand = brandResponse.localizedBrand,
            )
        }
    }
}
