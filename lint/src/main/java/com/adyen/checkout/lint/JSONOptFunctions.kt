/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/8/2024.
 */

package com.adyen.checkout.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

internal val JSON_OPT_FUNCTIONS_ISSUE = Issue.create(
    id = "JSONOptFunctions",
    briefDescription = "JSONObject \"opt\" functions should not be used directly",
    explanation = """
        JSONObject's optString, optBoolean, optInt, optLong and optDouble functions are non nullable and return
        arbitrary default values which could cause unexpected bugs. Use an internal extension function that returns
        null instead.
    """.trimIndent().replace(Regex("(\n*)\n"), "$1"),
    implementation = Implementation(JSONOptFunctionsDetector::class.java, Scope.JAVA_FILE_SCOPE),
    category = Category.CUSTOM_LINT_CHECKS,
    priority = 5,
    severity = Severity.ERROR,
    androidSpecific = true,
)

internal class JSONOptFunctionsDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(
        "optString", "optBoolean", "optInt", "optLong", "optDouble",
    )

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, "org.json.JSONObject")) return
        val methodName = node.methodIdentifier?.name.orEmpty()
        val replacement = when (methodName) {
            "optString" -> "getStringOrNull"
            "optBoolean" -> "getBooleanOrNull"
            "optInt" -> "getIntOrNull"
            "optLong" -> "getLongOrNull"
            "optDouble" -> "getDoubleOrNull"
            else -> return
        }
        context.report(
            JSON_OPT_FUNCTIONS_ISSUE,
            node,
            context.getLocation(node.methodIdentifier),
            "JSONObject.$methodName should not be used directly. Use an internal extension function instead.",
            fix()
                .alternatives(
                    // this replacement does not compile when 2 arguments are passed to the opt functions instead of 1.
                    // however, it's easy for the developer to fix it manually
                    fix()
                        .replace()
                        .all()
                        .with(replacement)
                        .imports("com.adyen.checkout.core.internal.data.model.$replacement")
                        .reformat(true)
                        .shortenNames()
                        .build(),
                ),
        )
    }
}
