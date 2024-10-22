package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutOverrideModifier
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withParentNamed
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

@Suppress("ConstPropertyName")
class DatabaseLayerTest {

    @Test
    fun `All 'Database' models should reside in 'database model' package`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = DatabaseModelSuffix)
            .assertTrue { declaration ->
                declaration.resideInPackage(name = DatabaseModelPackage)
            }
    }

    @Test
    fun `All 'Entity' models should reside in 'database model' package`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = DatabaseEntitySuffix)
            .assertTrue { declaration ->
                declaration.resideInPackage(name = DatabaseModelPackage)
            }
    }

    @Test
    fun `Classes representing 'database model' or 'entities' should be data classes with only vals`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage(DatabaseModelPackage)
            .assertTrue { declaration ->
                val isPublicOrDefault = declaration.hasPublicOrDefaultModifier
                val hasCorrectSuffix = declaration.name.endsWith(DatabaseModelSuffix) || declaration.name.endsWith(DatabaseEntitySuffix)
                val isDataClass = declaration.hasDataModifier
                val areFunctionsEmpty = declaration.functions().isEmpty()
                println(
                    "Class: ${declaration.name}, isPublicOrDefault: $isPublicOrDefault, " +
                        "hasCorrectSuffix: $hasCorrectSuffix, " +
                        "isDataClass: $isDataClass, " +
                        "areFunctionsEmpty: $areFunctionsEmpty.",
                )
                isPublicOrDefault &&
                    hasCorrectSuffix &&
                    isDataClass &&
                    declaration.properties().all { propertyDeclaration ->
                        val isVal = propertyDeclaration.hasValModifier
                        val isPropertyPublicOrDefault =
                            propertyDeclaration.hasPublicOrDefaultModifier
                        println(
                            "Property: ${propertyDeclaration.name}, isVal: $isVal, " +
                                "isPublicOrDefault: $isPropertyPublicOrDefault.",
                        )
                        isVal && isPropertyPublicOrDefault
                    } && areFunctionsEmpty
            }
    }

    @Test
    fun `'DatabaseDataSource' interface should be public and all functions with suspend`() {
        Konsist
            .scopeFromProduction()
            .interfaces()
            .withName(DatabaseDataSource)
            .assertTrue(strict = true) { declaration ->
                declaration.resideInPackage(name = DatabasePackage) &&
                        declaration.hasPublicOrDefaultModifier &&
                        declaration.functions().all { functionDeclaration ->
                            functionDeclaration.hasSuspendModifier
                        } &&
                        declaration.hasAllChildren(indirectChildren = true) { childDeclaration ->
                            childDeclaration.resideInPackage(name = DatabasePackage)
                        }
            }
    }

    @Test
    fun `Classes that implement 'DatabaseDataSource' should be internal with all properties private and val`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage(DatabasePackage)
            .withParentNamed(name = DatabaseDataSource)
            .assertTrue { declaration ->
                declaration.hasInternalModifier &&
                    declaration
                        .properties()
                        .withoutOverrideModifier()
                        .all { propertyDeclaration ->
                            propertyDeclaration.hasPrivateModifier &&
                                propertyDeclaration.hasValModifier
                        }
            }
    }

    companion object {
        private const val DatabaseModelSuffix = "DbModel"
        private const val DatabaseEntitySuffix = "Entity"
        private const val DatabaseModelPackage = "..database.model"
        private const val DatabasePackage = "..database"
        private const val DatabaseDataSource = "DatabaseDataSource"
    }
}
