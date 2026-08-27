/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

package com.adyen.checkout

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

val Project.libs
    get(): LibrariesForLibs = the<LibrariesForLibs>()

/**
 * The Maven group ID that all Checkout modules are published under.
 *
 * This is both the `groupId` of every publication and the Gradle group of every library module. The latter is what
 * allows Android Lint to tell library modules apart from consumer modules like the example app, which it needs in
 * order to enforce `@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)`.
 */
const val CHECKOUT_GROUP_ID = "com.adyen.checkout"
