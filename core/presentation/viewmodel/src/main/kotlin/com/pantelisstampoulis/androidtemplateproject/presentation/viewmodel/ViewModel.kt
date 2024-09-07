package com.pantelisstampoulis.androidtemplateproject.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import androidx.lifecycle.ViewModel as AndroidXViewModel
import androidx.lifecycle.viewModelScope as androidXViewModelScope

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
open class ViewModel(
    scope: CoroutineScope?,
) : AndroidXViewModel() {

    val viewModelScope: CoroutineScope = scope ?: androidXViewModelScope
}
