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
class NetworkLayerTest {

    @Test
    fun `All 'Api' models should reside in 'network model' package`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = NetworkModelSuffix)
            .assertTrue { declaration ->
                declaration.resideInPackage(name = NetworkModelPackage)
            }
    }

    @Test
    fun `Classes representing 'network model' should be data classes with only vals with kotlinx serialization`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage(NetworkModelPackage)
            .assertTrue { declaration ->
                val isPublicOrDefault = declaration.hasPublicOrDefaultModifier
                val hasCorrectSuffix = declaration.name.endsWith(NetworkModelSuffix)
                val isDataClass = declaration.hasDataModifier
                val hasSerializableAnnotation = declaration.hasAnnotationWithName(
                    name = "Serializable",
                )
                val areFunctionsEmpty = declaration.functions().isEmpty()
                println(
                    "Class: ${declaration.name}, isPublicOrDefault: $isPublicOrDefault, " +
                        "hasCorrectSuffix: $hasCorrectSuffix, " +
                        "isDataClass: $isDataClass, " +
                        "hasSerializableAnnotation: $hasSerializableAnnotation, " +
                        "areFunctionsEmpty: $areFunctionsEmpty.",
                )
                isPublicOrDefault &&
                    hasCorrectSuffix &&
                    isDataClass &&
                    hasSerializableAnnotation &&
                    declaration.properties().all { propertyDeclaration ->
                        val isVal = propertyDeclaration.hasValModifier
                        val isPropertyPublicOrDefault =
                            propertyDeclaration.hasPublicOrDefaultModifier
                        val hasSerialNameAnnotation =
                            propertyDeclaration.hasAnnotationWithName(name = "SerialName")
                        println(
                            "Property: ${propertyDeclaration.name}, isVal: $isVal, " +
                                "isPublicOrDefault: $isPropertyPublicOrDefault, " +
                                "hasSerialNameAnnotation: $hasSerialNameAnnotation.",
                        )
                        isVal &&
                            isPropertyPublicOrDefault &&
                            hasSerialNameAnnotation
                    } && areFunctionsEmpty
            }
    }

    @Test
    fun `'NetworkDataSource' interface should be public and all functions with suspend`() {
        Konsist
            .scopeFromProduction()
            .interfaces()
            .withName(NetworkDataSource)
            .assertTrue(strict = true) { declaration ->
                declaration.resideInPackage(name = NetworkPackage) &&
                    declaration.hasPublicOrDefaultModifier &&
                    declaration.functions().all { functionDeclaration ->
                        functionDeclaration.hasSuspendModifier
                    } &&
                    declaration.hasAllChildren(indirectChildren = true) { childDeclaration ->
                        childDeclaration.resideInPackage(name = NetworkPackage)
                    }
            }
    }

    @Test
    fun `Classes that implement 'NetworkDataSource' should be internal with all properties private and val`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withPackage(NetworkPackage)
            .withParentNamed(name = NetworkDataSource)
            .assertTrue { declaration ->
                declaration.hasInternalModifier
                declaration.properties().withoutOverrideModifier().all { propertyDeclaration ->
                    propertyDeclaration.hasPrivateModifier &&
                        propertyDeclaration.hasValModifier
                }
            }
    }

    companion object {
        private const val NetworkModelSuffix = "ApiModel"
        private const val NetworkModelPackage = "..network.model"
        private const val NetworkPackage = "..network"
        private const val NetworkDataSource = "NetworkDataSource"
    }
}
