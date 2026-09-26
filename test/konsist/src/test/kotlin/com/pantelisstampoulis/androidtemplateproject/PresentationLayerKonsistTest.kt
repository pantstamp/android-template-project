package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

@Suppress("ConstPropertyName")
class PresentationLayerKonsistTest {

    @Test
    fun `'ViewModels' classes should NOT use repositories directly`() {
        val viewModelScope = Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = ViewModelSuffix)
        viewModelScope
            .assertTrue { declaration ->
                // Get constructor parameters of the class
                val constructorParams = declaration.primaryConstructor?.parameters ?: emptyList()

                // Check that none of the parameters have types ending with "Repository"
                constructorParams.none { param ->
                    param.type.name.endsWith(RepositorySuffix)
                }
            }
    }

    /**
     * Screens take state, effects and callbacks, and nothing else. A Compose-side Koin lookup
     * (`getKoin()`, `koinInject()`) inside a composable throws in every `@Preview`, because a
     * preview never starts Koin, and `KoinApplicationPreview` only hides that. CI compiles
     * previews but never renders them, so the breakage is silent. Dependencies are resolved at
     * the navigation layer (`koinViewModel()` in `*Navigation.kt`), outside this package.
     *
     * The rule checks imports, so a fully qualified call with no import would bypass it.
     *
     * Every screen preview was broken this way until [issue #25][issue] was fixed.
     *
     * [issue]: https://github.com/pantstamp/android-template-project/issues/25
     */
    @Test
    fun `Feature presentation files should NOT import Koin's Compose APIs`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage(FeaturePresentationPackage)
            .assertFalse { file ->
                file.imports.any { import ->
                    KoinComposePackages.any { prefix -> import.name.startsWith(prefix) }
                }
            }
    }

    companion object {
        private const val ViewModelSuffix = "ViewModel"
        private const val RepositorySuffix = "Repository"
        private const val FeaturePresentationPackage = "..feature..presentation.."
        private val KoinComposePackages = listOf("org.koin.compose", "org.koin.androidx.compose")
    }
}
