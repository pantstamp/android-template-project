package com.pantelisstampoulis.androidtemplateproject.utils.koin

import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

inline fun <reified T> Scope.getWith(vararg params: Any?): T = get(parameters = { parametersOf(*params) })
