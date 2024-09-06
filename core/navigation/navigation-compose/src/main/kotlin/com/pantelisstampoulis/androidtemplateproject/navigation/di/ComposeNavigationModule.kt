package com.pantelisstampoulis.androidtemplateproject.navigation.di

import androidx.navigation.NavHostController
import com.pantelisstampoulis.androidtemplateproject.navigation.AndroidComposeNavigator
import com.pantelisstampoulis.androidtemplateproject.navigation.NavigationConstants
import com.pantelisstampoulis.androidtemplateproject.navigation.Navigator
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

val navigationModule: Module = module {

    factory<NavHostController> { getProperty(NavigationConstants.NAVIGATION_CONTROLLER) }

    factory {
        AndroidComposeNavigator(get())
    } bind Navigator::class
}
