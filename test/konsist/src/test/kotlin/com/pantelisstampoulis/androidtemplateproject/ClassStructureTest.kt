package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class ClassStructureTest {

    @Test
    fun `Companion object is last declaration in the class`() {
        Konsist
            .scopeFromProject()
            .classes()
            .assertTrue { declaration ->
                val companionObject = declaration
                    .objects(includeNested = false)
                    .lastOrNull { objectDeclaration ->
                        objectDeclaration.hasModifier(KoModifier.COMPANION)
                    }

                if (companionObject != null) {
                    declaration
                        .declarations(includeNested = false, includeLocal = false)
                        .last() == companionObject
                } else {
                    true
                }
            }
    }

    @Test
    fun `No field should have 'm' prefix`() {
        Konsist
            .scopeFromProject()
            .classes()
            .properties()
            .assertFalse { declaration ->
                val secondCharacterIsUppercase = declaration
                    .name
                    .getOrNull(index = 1)
                    ?.isUpperCase() ?: false
                declaration.name.startsWith(char = 'm') && secondCharacterIsUppercase
            }
    }
}
