/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/8/2024.
 */

package com.adyen.checkout.lint

import com.android.SdkConstants
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Attr

internal val TEXT_IN_LAYOUT_XML_ISSUE = Issue.create(
    id = "TextInLayoutXml",
    briefDescription = "Text should not be set directly",
    explanation = """
        Text should be defined in a style, so that merchants can easily override it.
    """.trimIndent().replace(Regex("(\n*)\n"), "$1"),
    implementation = Implementation(TextInLayoutXmlDetector::class.java, Scope.ALL_RESOURCES_SCOPE),
    category = Category.MESSAGES,
    priority = 5,
    severity = Severity.ERROR,
    androidSpecific = true,
)

internal class TextInLayoutXmlDetector : LayoutDetector() {

    override fun getApplicableAttributes(): Collection<String> = listOf(
        SdkConstants.ATTR_TEXT,
        SdkConstants.ATTR_HINT,
    )

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        if (isTools(attribute)) return

        context.report(
            TEXT_IN_LAYOUT_XML_ISSUE,
            attribute,
            context.getLocation(attribute),
            "Text should be defined in a style",
        )
    }

    private fun isTools(attribute: Attr): Boolean {
        return attribute.namespaceURI == SdkConstants.TOOLS_URI
    }
}
