package com.pantelisstampoulis.androidtemplateproject.navigation

import androidx.navigation.NavHostController

class AndroidComposeNavigator(private val navController: NavHostController): Navigator {

    override fun navigateTo(destination: Any) {
        navController.navigate(destination)
    }
}


