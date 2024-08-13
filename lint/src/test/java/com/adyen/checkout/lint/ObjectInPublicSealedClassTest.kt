package com.adyen.checkout.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

internal class ObjectInPublicSealedClassTest {

    @Test
    fun whenSealedClassIsPrivateOrInternal_thenIssueShouldNotBeDetected() {
        lint()
            .files(
                RESTRICT_TO_STUB,
                kotlin(
                    """
                    package test

                    private sealed class PrivateSealedClass {
                        data object Sub1 : PrivateSealedClass()
                        data class Sub2(val test: String) : PrivateSealedClass()
                    }
                    """,
                ).indented(),

                kotlin(
                    """
                    package test

                    internal sealed class InternalSealedClass {
                        data object Sub1 : InternalSealedClass()
                        data class Sub2(val test: String) : InternalSealedClass()
                    }
                    """,
                ).indented(),

                kotlin(
                    """
                    package test

                    import androidx.annotation.RestrictTo

                    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
                    sealed class RestrictedSealedClass {
                        data object Sub1 : RestrictedSealedClass()
                        data class Sub2(val test: String) : RestrictedSealedClass()
                    }
                    """,
                ).indented(),
            )
            .issues(OBJECT_IN_PUBLIC_SEALED_CLASS_ISSUE)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun whenPublicSealedClassHasObjectSubclass_thenIssueShouldBeDetected() {
        lint()
            .files(
                kotlin(
                    """
                    package test

                    sealed class PublicSealedClass1 {
                        data object Sub1 : PublicSealedClass1()
                        data class Sub2(val test: String) : PublicSealedClass1()
                    }
                    """,
                ).indented(),

                kotlin(
                    """
                    package test

                    sealed class PublicSealedClass2 {
                        object Sub1 : PublicSealedClass2()
                        data class Sub2(val test: String) : PublicSealedClass2()
                    }
                    """,
                ).indented(),
            )
            .issues(OBJECT_IN_PUBLIC_SEALED_CLASS_ISSUE)
            .allowMissingSdk()
            .run()
            .expect(
                """
                src/test/PublicSealedClass1.kt:4: Error: Don't use object, use class instead [ObjectInPublicSealedClass]
                    data object Sub1 : PublicSealedClass1()
                         ~~~~~~
                src/test/PublicSealedClass2.kt:4: Error: Don't use object, use class instead [ObjectInPublicSealedClass]
                    object Sub1 : PublicSealedClass2()
                    ~~~~~~
                2 errors, 0 warnings
                """,
            )
            .expectFixDiffs(
                """
                Fix for src/test/PublicSealedClass1.kt line 4: Replace with class:
                @@ -4 +4
                -     data object Sub1 : PublicSealedClass1()
                +     data class Sub1 : PublicSealedClass1()
                Fix for src/test/PublicSealedClass2.kt line 4: Replace with class:
                @@ -4 +4
                -     object Sub1 : PublicSealedClass2()
                +     class Sub1 : PublicSealedClass2()
                """,
            )
    }

    @Test
    fun whenPublicSealedClassHasNoObjectSubclass_thenIssueShouldNotBeDetected() {
        lint()
            .files(
                kotlin(
                    """
                    package test

                    sealed class PublicSealedClass1 {
                        data class Sub1(val test: String) : PublicSealedClass1()
                        data class Sub2(val test: String) : PublicSealedClass1()
                    }
                    """,
                ).indented(),
            )
            .issues(OBJECT_IN_PUBLIC_SEALED_CLASS_ISSUE)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    companion object {

        private val RESTRICT_TO_STUB = kotlin(
            """
            package androidx.annotation

            @Retention(AnnotationRetention.BINARY)
            annotation class RestrictTo
            """,
        ).indented()
    }
}
