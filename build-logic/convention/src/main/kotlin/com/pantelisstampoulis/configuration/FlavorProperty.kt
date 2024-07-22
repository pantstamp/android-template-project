package com.pantelisstampoulis.configuration

enum class FlavorProperty(
    val value: String,
    val modulePrefix: String,
    val variations: List<String>,
) {

    Database(
        value = DatabasePropertyValue,
        modulePrefix = DatabaseModulePrefix,
        variations = listOf(DatabaseSqlDelightFlavor, NoopFlavor),
    ),

    Logging(
        value = LoggingPropertyValue,
        modulePrefix = LoggingModulePrefix,
        variations = listOf(LoggingKermitFlavor, NoopFlavor),
    ),

    Network(
        value = NetworkPropertyValue,
        modulePrefix = NetworkModulePrefix,
        variations = listOf(NetworkKtorFlavor, NoopFlavor),
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
