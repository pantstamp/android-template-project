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
    fun `domain models use plain business names without technical suffix`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withPackage("com.pantelisstampoulis.androidtemplateproject.model..")
            .assertFalse { klass ->
                klass.name.endsWith("DomainModel") ||
                        klass.name.endsWith("ApiModel") ||
                        klass.name.endsWith("DbModel")
            }
    }

    @Test
    fun `data mappers implement one of the standard mapping interfaces`() {
        val standardMapperInterfaces = listOf(
            "ApiToDbMapper",
            "DbToDomainMapper",
            "ApiToDomainMapper",
            "DomainToUiMapper",
        )

        Konsist
            .scopeFromProject()
            .classes()
            .withPackage("com.pantelisstampoulis.androidtemplateproject.data.mapper..")
            .withNameEndingWith("Mapper")
            .filterNot { it.name == "ErrorDomainMapper" }
            .assertTrue { klass ->
                val parentInterfaces = klass.parentInterfaces(indirectParents = true)

               parentInterfaces.any { parent ->
                    standardMapperInterfaces.any { prefix ->
                        parent.name.startsWith(prefix)
                    }
                }
            }
    }


    @Test
    fun `clean architecture layers have correct dependencies`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                val domain = Layer(
                    name = "Domain",
                    rootPackage = "com.pantelisstampoulis.androidtemplateproject.model..",
                )
                val data = Layer(
                    name = "Data",
                    rootPackage = "com.pantelisstampoulis.androidtemplateproject.data..",
                )
                val network = Layer(
                    name = "Network",
                    rootPackage = "com.pantelisstampoulis.androidtemplateproject.network..",
                )
                val database = Layer(
                    name = "Database",
                    rootPackage = "com.pantelisstampoulis.androidtemplateproject.database..",
                )
                val ui = Layer(
                    name = "Ui",
                    rootPackage = "com.pantelisstampoulis.androidtemplateproject.ui..",
                )

                domain.dependsOnNothing()
                network.dependsOnNothing()
                database.dependsOnNothing()

                data.dependsOn(domain, network, database)
                ui.dependsOn(domain)
            }
    }

    @Test
    fun `repositories do not expose ApiModel or DbModel in public return types`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withPackage("com.pantelisstampoulis.androidtemplateproject.data.repository..")
            .functions()
            .assertFalse { function ->
                val returnTypeName = function
                    .returnType
                    ?.sourceDeclaration
                    ?.name
                    ?: ""

                function.hasPublicOrDefaultModifier &&
                        (returnTypeName.endsWith("ApiModel") || returnTypeName.endsWith("DbModel"))
            }
    }

    @Test
    fun `model suffixes match their designated package`() {
        val modelClasses = Konsist
            .scopeFromProject()
            .classes()
            .filter { klass ->
                klass.name.endsWith("ApiModel") ||
                        klass.name.endsWith("DbModel") ||
                        klass.name.endsWith("UiModel")
            }

        modelClasses.assertTrue { klass ->
            when {
                klass.name.endsWith("ApiModel") ->
                    klass.resideInPackage(
                        "com.pantelisstampoulis.androidtemplateproject.network.model..",
                    )

                klass.name.endsWith("DbModel") ->
                    klass.resideInPackage(
                        "com.pantelisstampoulis.androidtemplateproject.database.model..",
                    )

                klass.name.endsWith("UiModel") ->
                    klass.resideInPackage(
                        "..presentation.model..",
                    )

                else -> true // not our concern
            }
        }
    }

    @Test
    fun mapper_classes_should_implement_correct_mapper_interfaces() {
        Konsist
            .scopeFromProject()
            .classes()
            .assertTrue { koClass ->
                when {
                    // API → DB mappers
                    koClass.resideInPackage("..data.mapper..") &&
                            koClass.hasNameEndingWith("DataMapper") ->
                        koClass.hasParentInterfaceWithName(
                            "ApiToDbMapper",
                            "ApiToDomainMapper" // optional alternative if you use it
                        )

                    // DB → Domain mappers (excluding special-case ErrorDomainMapper)
                    koClass.resideInPackage("..data.mapper..") &&
                            koClass.hasNameEndingWith("DomainMapper") &&
                            koClass.name != "ErrorDomainMapper" ->
                        koClass.hasParentInterfaceWithName("DbToDomainMapper")

                    // Domain → UI mappers
                    koClass.resideInPackage("..presentation..mapper..") &&
                            koClass.hasNameEndingWith("UiMapper") ->
                        koClass.hasParentInterfaceWithName("DomainToUiMapper")

                    else -> true
                }
            }
    }

}

