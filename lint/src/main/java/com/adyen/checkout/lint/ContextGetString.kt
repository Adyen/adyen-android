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
import org.jetbrains.uast.getContainingUClass

internal val CONTEXT_GET_STRING_ISSUE = Issue.create(
    id = "ContextGetString",
    briefDescription = "Context.getString() should not be used directly",
    explanation = """
        Use localizedContext.getString() instead of context.getString to make sure strings are localized correctly.
    """.trimIndent().replace(Regex("(\n*)\n"), "$1"),
    implementation = Implementation(ContextGetStringDetector::class.java, Scope.JAVA_FILE_SCOPE),
    category = Category.I18N,
    priority = 5,
    severity = Severity.ERROR,
    androidSpecific = true,
)

internal class ContextGetStringDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(
        "getString",
    )

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, "android.content.Context")) return

        if (!isCalledInsideOfViewClass(context, node)) return

        val receiver = node.receiver?.asSourceString()
            // Ignore parenthesis
            ?.replace("(", "")
            ?.replace(")", "")

        if (receiver != "localizedContext") {
            context.report(
                CONTEXT_GET_STRING_ISSUE,
                node,
                context.getLocation(node.receiver),
                "context used instead of localizedContext",
                fix()
                    .alternatives(
                        fix().replace().with("localizedContext").build(),
                    ),
            )
        }
    }

    private fun isCalledInsideOfViewClass(context: JavaContext, node: UCallExpression): Boolean {
        return context.evaluator.extendsClass(node.receiver?.getContainingUClass(), "android.view.View")
    }
}
