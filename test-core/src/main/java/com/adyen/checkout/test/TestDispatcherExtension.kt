/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/8/2022.
 */

package com.adyen.checkout.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver

/**
 * JUnit 5 extension that replaces [Dispatchers.Main] with a test dispatcher. This gives control over how the dispatcher
 * executes it's work.
 *
 * Example:
 * ```
 * @ExtendWith(TestDispatcherExtension::class)
 * internal class ExampleTest {
 *
 *     @Test
 *     fun test(dispatcher: CoroutineDispatcher) = runTest(dispatcher) {
 *         ...
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherExtension : BeforeEachCallback, AfterEachCallback, ParameterResolver {

    override fun beforeEach(context: ExtensionContext) {
        val testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        context.getStore(NAMESPACE).put(DISPATCHER, testDispatcher)
    }

    override fun afterEach(context: ExtensionContext) {
        Dispatchers.resetMain()
        context.getStore(NAMESPACE).remove(DISPATCHER, TestDispatcher::class.java)
    }

    @Throws(ParameterResolutionException::class)
    @Suppress("NewApi")
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        // It's safe to ignore the warning on getType(), because this code is used in JVM unit tests and not
        // android related.
        parameterContext.parameter.type.isAssignableFrom(CoroutineDispatcher::class.java) ||
            parameterContext.parameter.type.isAssignableFrom(TestDispatcher::class.java)

    @Throws(ParameterResolutionException::class)
    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any =
        extensionContext.getStore(NAMESPACE).get(DISPATCHER)

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create("com.adyen.checkout")
        private const val DISPATCHER = "dispatcher"
    }
}
