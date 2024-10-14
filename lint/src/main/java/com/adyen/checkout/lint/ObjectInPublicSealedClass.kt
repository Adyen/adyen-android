/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/8/2024.
 */

package com.adyen.checkout.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

internal val OBJECT_IN_PUBLIC_SEALED_CLASS_ISSUE = Issue.create(
    id = "ObjectInPublicSealedClass",
    briefDescription = "Public sealed classes should not have object subclasses",
    explanation = """
        If later a (optional) parameter would be needed for this object, then it would have to be changed to a class.
        This would break the public contract.
    """.trimIndent().replace(Regex("(\n*)\n"), "$1"),
    implementation = Implementation(ObjectInPublicSealedClassDetector::class.java, Scope.JAVA_FILE_SCOPE),
    category = Category.CUSTOM_LINT_CHECKS,
    priority = 7,
    severity = Severity.ERROR,
)

internal class ObjectInPublicSealedClassDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
        UClass::class.java,
    )

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {
        override fun visitClass(node: UClass) {
            if (!isPublic(node)) return

            if (isCompanionObject(node)) return

            if (!hasSealedParent(node)) return

            if (isObject(node)) {
                val psiText = node.sourcePsi?.text.orEmpty()
                val objectString = "object"
                val startOfObject = psiText.indexOf(objectString)
                context.report(
                    OBJECT_IN_PUBLIC_SEALED_CLASS_ISSUE,
                    node,
                    context.getRangeLocation(node.sourcePsi!!, startOfObject, objectString.length),
                    "Don't use object, use class instead",
                    fix()
                        .alternatives(
                            fix().replace().with("class").build(),
                        ),
                )
            }
        }

        private fun isPublic(node: UClass): Boolean {
            val parent = node.uastParent
            if (parent is UClass) {
                return isPublic(parent)
            }

            return context.evaluator.isPublic(node) && !node.hasAnnotation("androidx.annotation.RestrictTo")
        }

        private fun isCompanionObject(node: UClass): Boolean {
            return context.evaluator.isCompanion(node)
        }

        private fun hasSealedParent(node: UClass): Boolean {
            return node.supers.any { context.evaluator.isSealed(it) }
        }

        private fun isObject(node: UClass): Boolean {
            return node.sourcePsi is KtObjectDeclaration
        }
    }
}
