package com.adyen.checkout.giftcard

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.giftcard.util.GiftCardBalanceResult
import com.adyen.checkout.giftcard.util.GiftCardBalanceUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class GiftCardBalanceUtilsTest {

    @Test
    fun checkBalance_LargerTransactionLimit_ExpectFullPayment() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = createAmount(1000),
            amountToBePaid = createAmount(10)
        )

        assert(result is GiftCardBalanceResult.FullPayment)
        require(result is GiftCardBalanceResult.FullPayment)
        assertEquals(10, result.amountPaid.value)
        assertEquals(90, result.remainingBalance.value)
    }

    @Test
    fun checkBalance_LargerBalance_ExpectFullPayment() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(1000),
            transactionLimit = createAmount(100),
            amountToBePaid = createAmount(10)
        )

        assert(result is GiftCardBalanceResult.FullPayment)
        require(result is GiftCardBalanceResult.FullPayment)
        assertEquals(10, result.amountPaid.value)
        assertEquals(990, result.remainingBalance.value)
    }

    @Test
    fun checkBalance_NullTransactionLimit_ExpectFullPayment() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = null,
            amountToBePaid = createAmount(10)
        )

        assert(result is GiftCardBalanceResult.FullPayment)
        require(result is GiftCardBalanceResult.FullPayment)
        assertEquals(10, result.amountPaid.value)
        assertEquals(90, result.remainingBalance.value)
    }

    @Test
    fun checkBalance_ZeroRemainingBalance_ExpectFullPayment() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = createAmount(100),
            amountToBePaid = createAmount(100)
        )

        assert(result is GiftCardBalanceResult.FullPayment)
        require(result is GiftCardBalanceResult.FullPayment)
        assertEquals(100, result.amountPaid.value)
        assertEquals(0, result.remainingBalance.value)
    }

    @Test
    fun checkBalance_LargerTransactionLimit_ExpectPartialPayment() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = createAmount(200),
            amountToBePaid = createAmount(1000)
        )

        assert(result is GiftCardBalanceResult.PartialPayment)
        require(result is GiftCardBalanceResult.PartialPayment)
        assertEquals(100, result.amountPaid.value)
        assertEquals(0, result.remainingBalance.value)
    }

    @Test
    fun checkBalance_LargerBalance_ExpectPartialPayment() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(200),
            transactionLimit = createAmount(100),
            amountToBePaid = createAmount(1000)
        )

        assert(result is GiftCardBalanceResult.PartialPayment)
        require(result is GiftCardBalanceResult.PartialPayment)
        assertEquals(100, result.amountPaid.value)
        assertEquals(100, result.remainingBalance.value)
    }

    @Test
    fun checkBalance_NullTransactionLimit_ExpectPartialPayment() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = null,
            amountToBePaid = createAmount(1000)
        )

        assert(result is GiftCardBalanceResult.PartialPayment)
        require(result is GiftCardBalanceResult.PartialPayment)
        assertEquals(100, result.amountPaid.value)
        assertEquals(0, result.remainingBalance.value)
    }

    @Test
    fun checkBalance_ZeroRemainingBalance_ExpectPartialPayment() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = createAmount(100),
            amountToBePaid = createAmount(200)
        )

        assert(result is GiftCardBalanceResult.PartialPayment)
        require(result is GiftCardBalanceResult.PartialPayment)
        assertEquals(100, result.amountPaid.value)
        assertEquals(0, result.remainingBalance.value)
    }

    @Test
    fun checkBalance_BalanceUSD_ExpectNonMatchingCurrencies() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100, "USD"),
            transactionLimit = createAmount(500),
            amountToBePaid = createAmount(200)
        )

        assert(result is GiftCardBalanceResult.NonMatchingCurrencies)
    }

    @Test
    fun checkBalance_TransactionLimitUSD_ExpectNonMatchingCurrencies() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = createAmount(500, "USD"),
            amountToBePaid = createAmount(200)
        )

        assert(result is GiftCardBalanceResult.NonMatchingCurrencies)
    }

    @Test
    fun checkBalance_AmountUSD_ExpectNonMatchingCurrencies() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = null,
            amountToBePaid = createAmount(200, "USD")
        )

        assert(result is GiftCardBalanceResult.NonMatchingCurrencies)
    }

    @Test
    fun checkBalance_EmptyAmount_ExpectZeroAmountToBePaid() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = createAmount(10),
            amountToBePaid = Amount.EMPTY
        )

        assert(result is GiftCardBalanceResult.ZeroAmountToBePaid)
    }

    @Test
    fun checkBalance_ZeroAmount_ExpectZeroAmountToBePaid() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(100),
            transactionLimit = createAmount(10),
            amountToBePaid = createAmount(0)
        )

        assert(result is GiftCardBalanceResult.ZeroAmountToBePaid)
    }

    @Test
    fun checkBalance_EmptyBalance_ExpectZeroBalance() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = Amount.EMPTY,
            transactionLimit = createAmount(10),
            amountToBePaid = createAmount(100)
        )

        assert(result is GiftCardBalanceResult.ZeroBalance)
    }

    @Test
    fun checkBalance_ZeroBalance_ExpectZeroBalance() {
        val result = GiftCardBalanceUtils.checkBalance(
            balance = createAmount(0),
            transactionLimit = createAmount(10),
            amountToBePaid = createAmount(100)
        )

        assert(result is GiftCardBalanceResult.ZeroBalance)
    }

    private fun createAmount(value: Int, currency: String = "EUR"): Amount {
        return Amount().apply {
            this.value = value
            this.currency = currency
        }
    }
}
