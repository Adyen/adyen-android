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
