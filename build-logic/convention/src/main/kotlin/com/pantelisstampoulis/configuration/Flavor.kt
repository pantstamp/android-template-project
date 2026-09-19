package com.pantelisstampoulis.configuration

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.LibraryProductFlavor
import com.android.build.api.dsl.ProductFlavor
import org.gradle.api.NamedDomainObjectContainer

@Suppress("EnumEntryName")
enum class FlavorDimension {
    // add here the flavour dimensions of the app, for example
    //   contentType
}

@Suppress("EnumEntryName")
enum class AppFlavor(val dimension: FlavorDimension, val applicationIdSuffix: String? = null) {
    // add here the flavours of the app, for example
    //   demo(FlavorDimension.contentType, applicationIdSuffix = ".demo"),
    //   prod(FlavorDimension.contentType)
}

// AGP 9 types productFlavors per extension (ApplicationProductFlavor vs LibraryProductFlavor),
// and CommonExtension only exposes it as an out-projected container, which cannot be created
// into. Hence one overload per extension over a shared, type-safe implementation.

fun configureFlavors(
    commonExtension: ApplicationExtension,
    flavorConfigurationBlock: ApplicationProductFlavor.(flavor: AppFlavor) -> Unit = {}
) {
    // Add all flavor dimensions, for example
    //   commonExtension.flavorDimensions += FlavorDimension.contentType.name

    commonExtension.productFlavors {
        createAppFlavors(this, flavorConfigurationBlock) { flavor, suffix ->
            flavor.applicationIdSuffix = suffix
        }
    }
}

fun configureFlavors(
    commonExtension: LibraryExtension,
    flavorConfigurationBlock: LibraryProductFlavor.(flavor: AppFlavor) -> Unit = {}
) {
    // Add all flavor dimensions, for example
    //   commonExtension.flavorDimensions += FlavorDimension.contentType.name

    commonExtension.productFlavors {
        // applicationIdSuffix is an application-only concept, so libraries ignore it
        createAppFlavors(this, flavorConfigurationBlock) { _, _ -> }
    }
}

private fun <T : ProductFlavor> createAppFlavors(
    container: NamedDomainObjectContainer<T>,
    flavorConfigurationBlock: T.(flavor: AppFlavor) -> Unit,
    applicationIdSuffixSetter: (flavor: T, suffix: String) -> Unit,
) {
    AppFlavor.entries.forEach { appFlavor ->
        container.create(appFlavor.name) {
            dimension = appFlavor.dimension.name
            flavorConfigurationBlock(appFlavor)
            appFlavor.applicationIdSuffix?.let { suffix ->
                applicationIdSuffixSetter(this, suffix)
            }
        }
    }
}
