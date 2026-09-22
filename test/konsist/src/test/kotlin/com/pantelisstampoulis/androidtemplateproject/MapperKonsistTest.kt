package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

/**
 * CLAUDE.md states: "All mappers implement typed interfaces from `:architecture:mapper`".
 *
 * That rule lived only in prose until this test existed, which is why [PR #14][pr] merged a
 * mapper that violated it — the review caught it, nothing enforced it, and it survived the
 * merge.
 *
 * The rule takes the name at its word: a class called `*Mapper` maps one model to another and
 * says so in its type. Anything that does a different job should carry a different name rather
 * than an exemption here — `ErrorClassifier` is the worked example, since it turns a
 * [NetworkResult][nr] envelope into an error category rather than mapping model to model.
 *
 * [pr]: https://github.com/pantstamp/android-template-project/pull/14
 * [nr]: com.pantelisstampoulis.androidtemplateproject.network.NetworkResult
 */
@Suppress("ConstPropertyName")
class MapperKonsistTest {

    @Test
    fun `Classes named '*Mapper' should implement a mapper interface from 'architecture mapper'`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith(suffix = MapperSuffix)
            .withoutName(*MapperHolders)
            .assertTrue { declaration ->
                // A parent's name carries its type arguments, e.g.
                // "DbToDomainMapper<WatchedMovieDbModel, WatchedMovie>", so compare on the
                // part before '<' rather than the whole string.
                val parents = declaration.parents().map { it.name.substringBefore(delimiter = '<') }
                println("Class: ${declaration.name}, parents: $parents.")
                parents.any { parentName -> parentName in MapperInterfaces }
            }
    }

    companion object {
        private const val MapperSuffix = "Mapper"

        /**
         * `Mappers` is a holder that groups the real mappers for injection. It maps nothing
         * itself, so the rule does not apply to it.
         */
        private val MapperHolders = arrayOf("Mappers")

        private val MapperInterfaces = setOf(
            "ApiToDomainMapper",
            "ApiToDbMapper",
            "DbToDomainMapper",
            "DbToEntityMapper",
            "DomainToUiMapper",
            "EntityToDbMapper",
        )
    }
}
