package com.adyen.checkout.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

internal class NotAdyenLogTest {

    @Test
    fun whenAndroidLogIsUsed_thenIssueIsDetected() {
        lint()
            .files(
                ANDROID_LOG_STUB,
                kotlin(
                    """
                    package test

                    import android.util.Log

                    fun androidLog() {
                        Log.v("tag", "message")
                        Log.d("tag", "message")
                        Log.i("tag", "message")
                        Log.w("tag", "message")
                        Log.e("tag", "message")
                        Log.wtf("tag", "message")
                    }
                    """,
                ).indented(),
            )
            .issues(NOT_ADYEN_LOG_ISSUE)
            .allowMissingSdk()
            .run()
            .expect(
                """
                src/test/test.kt:6: Error: Log used instead of adyenLog [NotAdyenLog]
                    Log.v("tag", "message")
                    ~~~~~~~~~~~~~~~~~~~~~~~
                src/test/test.kt:7: Error: Log used instead of adyenLog [NotAdyenLog]
                    Log.d("tag", "message")
                    ~~~~~~~~~~~~~~~~~~~~~~~
                src/test/test.kt:8: Error: Log used instead of adyenLog [NotAdyenLog]
                    Log.i("tag", "message")
                    ~~~~~~~~~~~~~~~~~~~~~~~
                src/test/test.kt:9: Error: Log used instead of adyenLog [NotAdyenLog]
                    Log.w("tag", "message")
                    ~~~~~~~~~~~~~~~~~~~~~~~
                src/test/test.kt:10: Error: Log used instead of adyenLog [NotAdyenLog]
                    Log.e("tag", "message")
                    ~~~~~~~~~~~~~~~~~~~~~~~
                src/test/test.kt:11: Error: Log used instead of adyenLog [NotAdyenLog]
                    Log.wtf("tag", "message")
                    ~~~~~~~~~~~~~~~~~~~~~~~~~
                6 errors, 0 warnings    
                """
            )
    }

    companion object {

        private val ANDROID_LOG_STUB = kotlin(
            """
            package android.util

            object Log {
                fun v(tag: String, msg: String, tr: Throwable? = null) {}
                fun d(tag: String, msg: String, tr: Throwable? = null) {}
                fun i(tag: String, msg: String, tr: Throwable? = null) {}
                fun w(tag: String, msg: String, tr: Throwable? = null) {}
                fun e(tag: String, msg: String, tr: Throwable? = null) {}
                fun wtf(tag: String, msg: String, tr: Throwable? = null) {}
            }
            """,
        ).indented()
    }
}
