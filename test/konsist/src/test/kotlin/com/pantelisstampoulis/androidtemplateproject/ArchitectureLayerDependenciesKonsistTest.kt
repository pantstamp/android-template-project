package com.pantelisstampoulis.androidtemplateproject

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import kotlin.test.Test

@Suppress("ConstPropertyName")
class ArchitectureLayerDependenciesKonsistTest {

    @Test
    fun `every architecture layer has only the correct dependencies`() {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                val networkLayer = Layer(name = NetworkLayer, rootPackage = NetworkPackage)
                val databaseLayer = Layer(name = DatabaseLayer, rootPackage = DatabasePackage)
                val modelLayer = Layer(name = ModelLayer, rootPackage = ModelPackage)
                val dataLayer = Layer(name = DataLayer, rootPackage = DataPackage)
                val domainLayer = Layer(name = DomainLayer, rootPackage = DomainPackage)
                val presentationLayer = Layer(name = PresentationLayer, rootPackage = PresentationPackage)
                val featureLayer = Layer(name = FeatureLayer, rootPackage = FeaturePackage)

                networkLayer.dependsOnNothing()

                databaseLayer.dependsOnNothing()

                modelLayer.dependsOnNothing()

                dataLayer.dependsOn(
                    networkLayer,
                    databaseLayer,
                    modelLayer,
                    domainLayer,
                )

                domainLayer.dependsOn(layer = modelLayer)

                featureLayer.dependsOn(modelLayer, domainLayer, presentationLayer)
            }
    }

    companion object {
        // packages
        private const val BasePackage = "com.pantelisstampoulis.androidtemplateproject"
        private const val NetworkPackage = "$BasePackage.network.."
        private const val DatabasePackage = "$BasePackage.database.."
        private const val ModelPackage = "$BasePackage.model.."
        private const val DomainPackage = "$BasePackage.domain.."
        private const val DataPackage = "$BasePackage.data.."
        private const val PresentationPackage = "$BasePackage.presentation.."
        private const val FeaturePackage = "$BasePackage.feature.."

        // layers
        private const val NetworkLayer = "Network"
        private const val DatabaseLayer = "Database"
        private const val DataLayer = "Data"
        private const val ModelLayer = "Model"
        private const val DomainLayer = "Domain"
        private const val PresentationLayer = "Presentation"
        private const val FeatureLayer = "Feature"
    }
}
