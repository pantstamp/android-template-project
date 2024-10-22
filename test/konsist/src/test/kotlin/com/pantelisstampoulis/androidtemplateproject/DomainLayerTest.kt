package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import com.pantelisstampoulis.androidtemplateproject.utils.Constants.InvokeFunction
import kotlin.test.Test

@Suppress("ConstPropertyName")
class DomainLayerTest {

    @Test
    fun `'UseCase' interfaces & classes should reside in 'domain usecase' package`() {
        val useCaseScope = Konsist
            .scopeFromProduction()
            .interfaces()
            .withNameEndingWith(suffix = UseCaseSuffix)
        val useCaseImplScope = Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = UseCaseImplSuffix)
        (useCaseScope + useCaseImplScope)
            .assertTrue { declaration ->
                declaration.resideInPackage(DomainUseCasePackage)
            }
    }

    @Test
    fun `Classes with 'UseCase' suffix should have single 'public operator' method named 'invoke'`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = UseCaseImplSuffix)
            .assertTrue { declaration ->
                val hasSingleInvokeOperatorMethod = declaration.hasFunction { function ->
                    function.let {
                        it.name == InvokeFunction &&
                                it.hasPublicOrDefaultModifier &&
                                it.hasOperatorModifier
                    }
                }

                hasSingleInvokeOperatorMethod &&
                    declaration.countFunctions { item -> item.hasPublicOrDefaultModifier } == 1
            }
    }

    @Test
    fun `'Repository' interfaces should reside in 'repository' package`() {
        val repositoryScope = Konsist
            .scopeFromProduction()
            .interfaces()
            .withNameEndingWith(suffix = RepositorySuffix)

        (repositoryScope)
            .assertTrue { declaration ->
                declaration.resideInPackage(RepositoryPackage)
            }
    }

    companion object {
        private const val UseCaseSuffix = "UseCase"
        private const val UseCaseImplSuffix = "UseCaseImpl"
        private const val DomainUseCasePackage = "..domain..usecase.."
        private const val RepositorySuffix = "Repository"
        private const val RepositoryPackage = "..domain..repository"
    }
}
