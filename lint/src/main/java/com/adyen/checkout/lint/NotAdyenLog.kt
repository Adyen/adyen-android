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

internal val NOT_ADYEN_LOG_ISSUE = Issue.create(
    id = "NotAdyenLog",
    briefDescription = "Log used instead of adyenLog",
    explanation = "adyenLog should be used, so we have control over the logs.",
    implementation = Implementation(NotAdyenLogDetector::class.java, Scope.JAVA_FILE_SCOPE),
    category = Category.MESSAGES,
    priority = 5,
    severity = Severity.ERROR,
)

internal class NotAdyenLogDetector : Detector(), Detector.UastScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(
        "v", "d", "i", "w", "e", "wtf",
    )

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (context.evaluator.isMemberInClass(method, "android.util.Log")) {
            context.report(
                NOT_ADYEN_LOG_ISSUE,
                node,
                context.getLocation(node),
                "Log used instead of adyenLog",
            )
        }
    }
}
