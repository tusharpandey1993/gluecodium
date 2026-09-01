/*
 * Copyright (C) 2016-2025 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.gluecodium.validator

import com.here.gluecodium.common.LimeLogger
import com.here.gluecodium.model.lime.LimeAttributeType.KOTLIN
import com.here.gluecodium.model.lime.LimeAttributeValueType.DATA_CLASS
import com.here.gluecodium.model.lime.LimeClass
import com.here.gluecodium.model.lime.LimeModel

/**
 * Validates that `@Kotlin(DataClass)` is only applied to LimeIDL `struct` elements, never to `class`
 * elements.
 *
 * Rationale: the C++/JNI code generator selects its instance-construction strategy purely on the
 * LimeIDL element type (`LimeStruct` vs. `LimeClass`), independent of any Kotlin-specific attribute.
 * `LimeStruct` instances are always constructed on the native->Kotlin path via `alloc_object` plus
 * direct field writes (no Kotlin constructor is ever invoked). `LimeClass` instances are always
 * constructed via `create_instance_object`, which looks up the NativeBase constructor with signature
 * `(long, Object)`.
 *
 * A Kotlin `data class` generated from `KotlinDataClass.mustache` has only the fields-based primary
 * constructor and no `(Long, Any?)` constructor. If `@Kotlin(DataClass)` is applied to a `class`, the
 * generated C++ still calls `create_instance_object`, which fails to find the `(long, Object)`
 * constructor at runtime and crashes with `NoSuchMethodError` inside `JNI NewObjectV`. This is a
 * deterministic runtime crash, not a build-time error, so it must be caught here instead.
 */
internal class LimeKotlinDataClassValidator(private val logger: LimeLogger) {
    fun validate(limeModel: LimeModel): Boolean {
        val offendingClasses =
            limeModel.referenceMap.values
                .filterIsInstance<LimeClass>()
                .filter { it.attributes.have(KOTLIN, DATA_CLASS) }

        for (limeClass in offendingClasses) {
            logger.error(
                limeClass,
                "@Kotlin(DataClass) is not supported on 'class' elements, only on 'struct' elements. " +
                    "Applying it to a class generates a Kotlin data class with no NativeBase " +
                    "(Long, Any?) constructor, while the C++/JNI layer still calls the NativeBase " +
                    "'create_instance_object' construction path for classes; this fails with " +
                    "'NoSuchMethodError' / 'JNI NewObjectV' at runtime when native code constructs an " +
                    "instance. Convert this LimeIDL element to a 'struct' to use @Kotlin(DataClass).",
            )
        }

        return offendingClasses.isEmpty()
    }
}
