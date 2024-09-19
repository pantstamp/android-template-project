# Convention Plugins

The `build-logic` directory contains project-specific convention plugins designed to maintain a centralized configuration approach for common module settings and is based on
[https://developer.squareup.com/blog/herding-elephants/](https://developer.squareup.com/blog/herding-elephants/)
and
[https://github.com/jjohannes/idiomatic-gradle](https://github.com/jjohannes/idiomatic-gradle).

By using convention plugins within `build-logic`, we eliminate redundant build script configurations and avoid clutter in subproject settings, overcoming the limitations of the buildSrc directory.

The `build-logic` folder is included as a build in the main settings.gradle.kts file.

Within `build-logic`, there is a convention module that defines plugins available to standard modules for their configurations.

Additionally, `build-logic` includes several Kotlin files to facilitate sharing logic between plugins, particularly useful when setting up Android components (libraries vs. applications) with shared configurations.

These plugins are modular and designed to handle specific tasks, allowing each module to apply only the configurations it requires. For any module that needs unique logic not shared with others, it’s recommended to define that directly in its build.gradle file, rather than creating a separate convention plugin for that specific use.

List of convention plugins:

- [`AndroidApplicationCorePlugin`](convention/src/main/kotlin/com/pantelisstampoulis/plugin/AndroidApplicationCorePlugin.kt):
  Configures common Android application options.
- [`AndroidApplicationComposePlugin`](convention/src/main/kotlin/com/pantelisstampoulis/plugin/AndroidApplicationComposePlugin.kt): Configures Compose Android application options.
- [`LibraryCorePlugin`](convention/src/main/kotlin/com/pantelisstampoulis/plugin/LibraryCorePlugin.kt): Configures common options for an Android library module.
- [`LibraryFeaturePlugin`](convention/src/main/kotlin/com/pantelisstampoulis/plugin/LibraryFeaturePlugin.kt): Configures common options for an Android library module that represents a feature.
- Other plugins that configure options for 3rd party libraries and tools like, [`ComposePlugin`](convention/src/main/kotlin/com/pantelisstampoulis/plugin/ComposePlugin.kt), [`KoinPlugin`](convention/src/main/kotlin/com/pantelisstampoulis/plugin/KoinPlugin.kt), [`RoomPlugin`](convention/src/main/kotlin/com/pantelisstampoulis/plugin/RoomPlugin.kt), etc. 

 
 