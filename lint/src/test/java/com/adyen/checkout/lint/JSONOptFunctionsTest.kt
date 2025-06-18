package com.adyen.checkout.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class JSONOptFunctionsTest {

    @Test
    fun whenJSONObjectOptFunctionsAreUsed_thenIssueIsDetected() {
        lint()
            .files(
                JSON_OBJECT_STUB,
                kotlin(
                    """
                    package test
                    
                    import org.json.JSONObject
                    
                    class SomeClass {
                        fun someFun(jsonObject: JSONObject) {
                            val stringWithoutFallback = jsonObject.optString("key")
                            val stringWithFallback = jsonObject.optString("key", "fallback")
                            val intWithoutFallback = jsonObject.optInt("key")
                            val intWithFallback = jsonObject.optInt("key", 1)
                            val doubleWithoutFallback = jsonObject.optDouble("key")
                            val doubleWithFallback = jsonObject.optDouble("key", 1.0)
                            val longWithoutFallback = jsonObject.optLong("key")
                            val longWithFallback = jsonObject.optLong("key", 1L)
                            val booleanWithoutFallback = jsonObject.optBoolean("key")
                            val booleanWithFallback = jsonObject.optBoolean("key", true)
                        }
                    }
                    """.trimIndent(),
                ),
            )
            .issues(JSON_OPT_FUNCTIONS_ISSUE)
            .allowMissingSdk()
            .run()
            .expect(
                """
src/test/SomeClass.kt:7: Error: JSONObject.optString should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val stringWithoutFallback = jsonObject.optString("key")
                                               ~~~~~~~~~
src/test/SomeClass.kt:8: Error: JSONObject.optString should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val stringWithFallback = jsonObject.optString("key", "fallback")
                                            ~~~~~~~~~
src/test/SomeClass.kt:9: Error: JSONObject.optInt should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val intWithoutFallback = jsonObject.optInt("key")
                                            ~~~~~~
src/test/SomeClass.kt:10: Error: JSONObject.optInt should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val intWithFallback = jsonObject.optInt("key", 1)
                                         ~~~~~~
src/test/SomeClass.kt:11: Error: JSONObject.optDouble should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val doubleWithoutFallback = jsonObject.optDouble("key")
                                               ~~~~~~~~~
src/test/SomeClass.kt:12: Error: JSONObject.optDouble should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val doubleWithFallback = jsonObject.optDouble("key", 1.0)
                                            ~~~~~~~~~
src/test/SomeClass.kt:13: Error: JSONObject.optLong should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val longWithoutFallback = jsonObject.optLong("key")
                                             ~~~~~~~
src/test/SomeClass.kt:14: Error: JSONObject.optLong should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val longWithFallback = jsonObject.optLong("key", 1L)
                                          ~~~~~~~
src/test/SomeClass.kt:15: Error: JSONObject.optBoolean should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val booleanWithoutFallback = jsonObject.optBoolean("key")
                                                ~~~~~~~~~~
src/test/SomeClass.kt:16: Error: JSONObject.optBoolean should not be used directly. Use an internal extension function instead. [JSONOptFunctions]
        val booleanWithFallback = jsonObject.optBoolean("key", true)
                                             ~~~~~~~~~~
10 errors, 0 warnings
                """.trimIndent(),
            )
            .expectFixDiffs(
                """
Fix for src/test/SomeClass.kt line 7: Replace with getStringOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
@@ -7 +8
-         val stringWithoutFallback = jsonObject.optString("key")
+         val stringWithoutFallback = jsonObject.getStringOrNull("key")
Fix for src/test/SomeClass.kt line 8: Replace with getStringOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
@@ -8 +9
-         val stringWithFallback = jsonObject.optString("key", "fallback")
+         val stringWithFallback = jsonObject.getStringOrNull("key", "fallback")
Fix for src/test/SomeClass.kt line 9: Replace with getIntOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getIntOrNull
@@ -9 +10
-         val intWithoutFallback = jsonObject.optInt("key")
+         val intWithoutFallback = jsonObject.getIntOrNull("key")
Fix for src/test/SomeClass.kt line 10: Replace with getIntOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getIntOrNull
@@ -10 +11
-         val intWithFallback = jsonObject.optInt("key", 1)
+         val intWithFallback = jsonObject.getIntOrNull("key", 1)
Fix for src/test/SomeClass.kt line 11: Replace with getDoubleOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getDoubleOrNull
@@ -11 +12
-         val doubleWithoutFallback = jsonObject.optDouble("key")
+         val doubleWithoutFallback = jsonObject.getDoubleOrNull("key")
Fix for src/test/SomeClass.kt line 12: Replace with getDoubleOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getDoubleOrNull
@@ -12 +13
-         val doubleWithFallback = jsonObject.optDouble("key", 1.0)
+         val doubleWithFallback = jsonObject.getDoubleOrNull("key", 1.0)
Fix for src/test/SomeClass.kt line 13: Replace with getLongOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getLongOrNull
@@ -13 +14
-         val longWithoutFallback = jsonObject.optLong("key")
+         val longWithoutFallback = jsonObject.getLongOrNull("key")
Fix for src/test/SomeClass.kt line 14: Replace with getLongOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getLongOrNull
@@ -14 +15
-         val longWithFallback = jsonObject.optLong("key", 1L)
+         val longWithFallback = jsonObject.getLongOrNull("key", 1L)
Fix for src/test/SomeClass.kt line 15: Replace with getBooleanOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getBooleanOrNull
@@ -15 +16
-         val booleanWithoutFallback = jsonObject.optBoolean("key")
+         val booleanWithoutFallback = jsonObject.getBooleanOrNull("key")
Fix for src/test/SomeClass.kt line 16: Replace with getBooleanOrNull:
@@ -3 +3
+ import com.adyen.checkout.core.old.internal.data.model.getBooleanOrNull
@@ -16 +17
-         val booleanWithFallback = jsonObject.optBoolean("key", true)
+         val booleanWithFallback = jsonObject.getBooleanOrNull("key", true)
                """.trimIndent(),
            )
    }

    companion object {

        private val JSON_OBJECT_STUB = java(
            """
            package org.json;
            
            public class JSONObject {
            
                @NonNull public String optString(@Nullable String name) {
                    return "stub";
                }
                    
                @NonNull public String optString(@Nullable String name, @NonNull String fallback) {
                    return "stub";
                }
                
                public int optInt(@Nullable String name) {
                    return 0;
                }
                
                public int optInt(@Nullable String name, int fallback) {
                    return 0;
                }
                        
                public long optLong(@Nullable String name) {
                    return 0L;
                }
                
                public long optLong(@Nullable String name, long fallback) {
                    return 0L;
                }            
                
                public double optDouble(@Nullable String name) {
                    return Double.NaN;
                }
                
                public double optDouble(@Nullable String name, double fallback) {
                    return Double.NaN;
                }
                
                public boolean optBoolean(@Nullable String name) {
                    return false;
                }
                
                public boolean optBoolean(@Nullable String name, boolean fallback) {
                    return false;
                }
            }
            """.trimIndent(),
        )
    }
}
