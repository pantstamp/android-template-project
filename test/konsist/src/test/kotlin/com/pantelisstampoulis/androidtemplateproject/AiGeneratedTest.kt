package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class AiGeneratedTest {

        @Test
        fun `domain and data models follow naming conventions`() {
            // Domain models: plain business names (no technical suffixes)
            Konsist
                .scopeFromProject()
                .classes()
                .filter { it.resideInPackage("com.pantelisstampoulis.androidtemplateproject.model.movies..") }
                .assertTrue { klass ->
                    !klass.name.endsWith("DomainModel") &&
                            !klass.name.endsWith("ApiModel") &&
                            !klass.name.endsWith("DbModel")
                }

            // Network models: must end with ApiModel
            Konsist
                .scopeFromProject()
                .classes()
                .filter { it.resideInPackage("com.pantelisstampoulis.androidtemplateproject.network.model..") }
                .assertTrue { klass ->
                    klass.name.endsWith("ApiModel")
                }

            // Database models: must end with DbModel
            Konsist
                .scopeFromProject()
                .classes()
                .filter { it.resideInPackage("com.pantelisstampoulis.androidtemplateproject.database.model..") }
                .assertTrue { klass ->
                    klass.name.endsWith("DbModel") || klass.name.endsWith("Entity")
                }
        }


}

