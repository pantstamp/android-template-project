package com.pantelisstampoulis.configuration

enum class FlavorProperty(
    val value: String,
    val modulePrefix: String,
    val variations: List<String>,
) {

    Database(
        value = DatabasePropertyValue,
        modulePrefix = DatabaseModulePrefix,
        variations = listOf(DatabaseRoomFlavor, NoopFlavor),
    ),

    Logging(
        value = LoggingPropertyValue,
        modulePrefix = LoggingModulePrefix,
        variations = listOf(NoopFlavor),
    ),

    Network(
        value = NetworkPropertyValue,
        modulePrefix = NetworkModulePrefix,
        variations = listOf(NetworkRetrofitFlavor, NoopFlavor),
    ),

    Navigation(
        value = NavigationPropertyValue,
        modulePrefix = NavigationModulePrefix,
        variations = listOf(NavigationComposeFlavor),
    ),

    Preferences(
        value = PreferencesPropertyValue,
        modulePrefix = PreferencesModulePrefix,
        variations = listOf(PreferencesDatastoreFlavor, NoopFlavor),
    );

    internal fun getFlavoredModule(flavor: String): String {
        require(value = flavor in variations) {
            "Provided flavor: $flavor not in available variations: $variations for ${this.name}"
        }
        return "$modulePrefix$flavor"
    }
}
