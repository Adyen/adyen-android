package com.adyen.checkout.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

internal class NotDispatcherProviderTest {

    @Test
    fun whenDispatchersIsUsed_thenIssueIsDetected() {
        lint()
            .files(
                DISPATCHERS_STUB,
                DISPATCHER_PROVIDER_STUB,
                kotlin(
                    """
                    package test

                    import kotlinx.coroutines.Dispatchers
                    import kotlinx.coroutines.CoroutineDispatcher
                    import com.adyen.checkout.core.DispatcherProvider

                    class Test(
                        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
                    ) {
                    
                        fun test() = withContext(Dispatchers.Default) {
                            withContext(DispatcherProvider.Main) {}
                        }
                    }
                    """,
                ).indented(),
            )
            .issues(NOT_DISPATCHER_PROVIDER_ISSUE)
            .allowMissingSdk()
            .allowDuplicates()
            .run()
            .expect(
                """
                src/test/Test.kt:8: Error: Dispatchers used instead of DispatcherProvider [NotDispatcherProvider]
                    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
                                                                  ~~~~~~~~~~~
                src/test/Test.kt:11: Error: Dispatchers used instead of DispatcherProvider [NotDispatcherProvider]
                    fun test() = withContext(Dispatchers.Default) {
                                             ~~~~~~~~~~~
                2 errors, 0 warnings
                """,
            )
            .expectFixDiffs(
                """
                Fix for src/test/Test.kt line 8: Replace with DispatcherProvider:
                @@ -8 +8
                -     private val dispatcher: CoroutineDispatcher = Dispatchers.IO
                +     private val dispatcher: CoroutineDispatcher = DispatcherProvider.IO
                Fix for src/test/Test.kt line 11: Replace with DispatcherProvider:
                @@ -11 +11
                -     fun test() = withContext(Dispatchers.Default) {
                +     fun test() = withContext(DispatcherProvider.Default) {
                """,
            )
    }

    companion object {

        private val DISPATCHERS_STUB = kotlin(
            """
            package kotlinx.coroutines

            object Dispatchers {
                 val Main = CoroutineDispatcher()
                 val Default = CoroutineDispatcher()
                 val IO = CoroutineDispatcher()
            }

            open class CoroutineDispatcher

            fun withContext(dispatcher: CoroutineDispatcher, block: () -> Unit) {
                block()
            }
            """,
        ).indented()

        private val DISPATCHER_PROVIDER_STUB = kotlin(
            """
            package com.adyen.checkout.core

            object DispatcherProvider {
                 val Main = CoroutineDispatcher()
                 val Default = CoroutineDispatcher()
                 val IO = CoroutineDispatcher()
            }
            """,
        ).indented()
    }
}
