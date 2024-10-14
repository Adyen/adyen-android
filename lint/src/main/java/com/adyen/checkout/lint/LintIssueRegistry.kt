/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/8/2024.
 */

package com.adyen.checkout.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

@Suppress("unused")
internal class LintIssueRegistry : IssueRegistry() {

    override val api: Int = CURRENT_API

    override val issues: List<Issue> = listOf(
        CONTEXT_GET_STRING_ISSUE,
        NOT_ADYEN_LOG_ISSUE,
        OBJECT_IN_PUBLIC_SEALED_CLASS_ISSUE,
        TEXT_IN_LAYOUT_XML_ISSUE,
    )
}
