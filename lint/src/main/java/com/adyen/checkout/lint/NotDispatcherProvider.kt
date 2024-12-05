/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/11/2024.
 */

package com.adyen.checkout.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UReferenceExpression
import org.jetbrains.uast.getQualifiedName

internal val NOT_DISPATCHER_PROVIDER_ISSUE = Issue.create(
    id = "NotDispatcherProvider",
    briefDescription = "Dispatchers used instead of DispatcherProvider",
    explanation = "DispatcherProvider should be used, so we can override dispatchers for testing purposes.",
    implementation = Implementation(NotDispatcherProvider::class.java, Scope.JAVA_FILE_SCOPE),
    category = Category.CUSTOM_LINT_CHECKS,
    priority = 7,
    severity = Severity.ERROR,
)

internal class NotDispatcherProvider : Detector(), SourceCodeScanner {

    override fun getApplicableReferenceNames() = listOf(
        "Dispatchers",
    )

    override fun visitReference(context: JavaContext, reference: UReferenceExpression, referenced: PsiElement) {
        if (reference.getQualifiedName() == "kotlinx.coroutines.Dispatchers") {
            context.report(
                NOT_DISPATCHER_PROVIDER_ISSUE,
                reference,
                context.getLocation(reference),
                "Dispatchers used instead of DispatcherProvider",
                fix().alternatives(
                    fix()
                        .replace()
                        .with("DispatcherProvider")
                        .imports("com.adyen.checkout.core.DispatcherProvider")
                        .reformat(true)
                        .shortenNames()
                        .build(),
                ),
            )
        }
    }
}
