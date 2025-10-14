package com.adyen.checkout.components.core

import com.adyen.checkout.components.core.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PaymentComponentDataTest {

    @Test
    fun `when serializing, then all fields should be serialized correctly`() {
        val paymentMethod = GenericPaymentMethod("type", "checkoutAttemptId", "subtype")
        val order = OrderRequest("pspReference", "orderData")
        val amount = Amount("EUR", 1L)
        val billingAddress = Address(city = "city")
        val deliveryAddress = Address(street = "street")
        val shopperName = ShopperName(firstName = "firstName")
        val installments = Installments("plan", 2)
        val request = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = amount,
            storePaymentMethod = true,
            shopperReference = "shopperReference",
            billingAddress = billingAddress,
            deliveryAddress = deliveryAddress,
            shopperName = shopperName,
            telephoneNumber = "telephoneNumber",
            shopperEmail = "shopperEmail",
            dateOfBirth = "dateOfBirth",
            socialSecurityNumber = "socialSecurityNumber",
            installments = installments,
            supportNativeRedirect = true,
            sdkData = "sdkData",
        )

        val actual = PaymentComponentData.SERIALIZER.serialize(request)

        val expected = JSONObject()
            .put("paymentMethod", PaymentMethodDetails.SERIALIZER.serialize(paymentMethod))
            .put("order", OrderRequest.SERIALIZER.serialize(order))
            .put("amount", Amount.SERIALIZER.serialize(amount))
            .put("storePaymentMethod", true)
            .put("shopperReference", "shopperReference")
            .put("billingAddress", Address.SERIALIZER.serialize(billingAddress))
            .put("deliveryAddress", Address.SERIALIZER.serialize(deliveryAddress))
            .put("shopperName", ShopperName.SERIALIZER.serialize(shopperName))
            .put("telephoneNumber", "telephoneNumber")
            .put("shopperEmail", "shopperEmail")
            .put("dateOfBirth", "dateOfBirth")
            .put("socialSecurityNumber", "socialSecurityNumber")
            .put("installments", Installments.SERIALIZER.serialize(installments))
            .put("supportNativeRedirect", true)
            .put("sdkData", "sdkData")

        assertEquals(expected.toString(), actual.toString())
    }

    @Test
    fun `when deserializing, then all fields should be deserializing correctly`() {
        val paymentMethod = GenericPaymentMethod("type", "checkoutAttemptId", "subtype")
        val order = OrderRequest("pspReference", "orderData")
        val amount = Amount("EUR", 1L)
        val billingAddress = Address(city = "city")
        val deliveryAddress = Address(street = "street")
        val shopperName = ShopperName(firstName = "firstName")
        val installments = Installments("plan", 2)
        val response = JSONObject()
            .put("paymentMethod", PaymentMethodDetails.SERIALIZER.serialize(paymentMethod))
            .put("order", OrderRequest.SERIALIZER.serialize(order))
            .put("amount", Amount.SERIALIZER.serialize(amount))
            .put("storePaymentMethod", true)
            .put("shopperReference", "shopperReference")
            .put("billingAddress", Address.SERIALIZER.serialize(billingAddress))
            .put("deliveryAddress", Address.SERIALIZER.serialize(deliveryAddress))
            .put("shopperName", ShopperName.SERIALIZER.serialize(shopperName))
            .put("telephoneNumber", "telephoneNumber")
            .put("shopperEmail", "shopperEmail")
            .put("dateOfBirth", "dateOfBirth")
            .put("socialSecurityNumber", "socialSecurityNumber")
            .put("installments", Installments.SERIALIZER.serialize(installments))
            .put("supportNativeRedirect", true)
            .put("sdkData", "sdkData")

        val actual = PaymentComponentData.SERIALIZER.deserialize(response)

        val expected = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = amount,
            storePaymentMethod = true,
            shopperReference = "shopperReference",
            billingAddress = billingAddress,
            deliveryAddress = deliveryAddress,
            shopperName = shopperName,
            telephoneNumber = "telephoneNumber",
            shopperEmail = "shopperEmail",
            dateOfBirth = "dateOfBirth",
            socialSecurityNumber = "socialSecurityNumber",
            installments = installments,
            supportNativeRedirect = true,
            sdkData = "sdkData",
        )

        assertEquals(expected, actual)
    }
}
