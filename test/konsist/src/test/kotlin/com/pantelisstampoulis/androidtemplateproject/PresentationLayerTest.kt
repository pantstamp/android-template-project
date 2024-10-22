package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

@Suppress("ConstPropertyName")
class PresentationLayerTest {

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

    companion object {
        private const val ViewModelSuffix = "ViewModel"
        private const val RepositorySuffix = "Repository"
    }
}
