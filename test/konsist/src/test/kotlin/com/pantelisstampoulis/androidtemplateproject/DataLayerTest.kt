package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import com.pantelisstampoulis.androidtemplateproject.utils.Constants.TestFileSuffix
import kotlin.test.Test

@Suppress("ConstPropertyName")
class DataLayerTest {

    @Test
    fun `'Repository' implementations should reside in 'repository' package`() {
        val repositoryImplScope = Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = RepositoryImplSuffix)

        (repositoryImplScope)
            .assertTrue { declaration ->
                declaration.resideInPackage(RepositoryPackage)
            }
    }

    @Test
    fun `Every Repository Implementation class has test`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = RepositoryImplSuffix)
            .assertTrue(strict = true) { declaration ->
                declaration.hasTestClass { classDeclaration ->
                    classDeclaration.name == declaration.name + TestFileSuffix
                }
            }
    }

    companion object {
        private const val RepositoryImplSuffix = "RepositoryImpl"
        private const val RepositoryPackage = "..data..repository"
    }
}
