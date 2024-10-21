package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class UseCasesResideInTheRightPackageTest {
    @Test
    fun `every use case reside in use case package`() {
        Konsist
            .scopeFromProject() // Define the scope containing all Kotlin files present in the project
            .classes() // Get all class declarations
            .withNameEndingWith("UseCaseImpl") // Filter classes heaving name ending with 'UseCase'
            .assertTrue { it.resideInPackage("..domain.usecase..") } // Assert that each class resides in 'any domain.usecase any' package
    }
}
