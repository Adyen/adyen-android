package com.adyen.checkout.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class ContextGetStringTest {

    @Test
    fun whenContextGetStringIsUsedInViewClass_thenIssueIsDetected() {
        lint()
            .files(
                CONTEXT_STUB,
                VIEW_STUB,
                kotlin(
                    """
                    package test
                    
                    import android.content.Context
                    import android.view.View
                    
                    class MyView : View() {
                        fun initialize(context: Context) {
                            context.getString(0)
                        }
                    }
                    """.trimIndent(),
                ),
                // Check if deep inheritance works as well
                kotlin(
                    """
                    package test
                    
                    import android.content.Context
                    import android.view.LinearLayout
                    
                    class MyLayout : LinearLayout() {
                        fun initialize(context: Context) {
                            context.getString(1)
                        }
                    }
                    """.trimIndent(),
                ),
            )
            .issues(CONTEXT_GET_STRING_ISSUE)
            .allowMissingSdk()
            .run()
            .expect(
                """
                src/test/MyLayout.kt:8: Error: context used instead of localizedContext [ContextGetString]
                        context.getString(1)
                        ~~~~~~~
                src/test/MyView.kt:8: Error: context used instead of localizedContext [ContextGetString]
                        context.getString(0)
                        ~~~~~~~
                2 errors, 0 warnings
                """.trimIndent(),
            )
            .expectFixDiffs(
                """
                Fix for src/test/MyLayout.kt line 8: Replace with localizedContext:
                @@ -8 +8
                -         context.getString(1)
                +         localizedContext.getString(1)
                Fix for src/test/MyView.kt line 8: Replace with localizedContext:
                @@ -8 +8
                -         context.getString(0)
                +         localizedContext.getString(0)
                """.trimIndent(),
            )
    }

    companion object {

        private val CONTEXT_STUB = kotlin(
            """
            package android.content
            
            class Context {
            
                fun getString(resId: Int): String = "stub"
            }
            """.trimIndent(),
        )

        private val VIEW_STUB = kotlin(
            """
            package android.view
            
            open class View
            open class ViewGroup : View()
            open class LinearLayout : ViewGroup()
            """,
        )
    }
}
