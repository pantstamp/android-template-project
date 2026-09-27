package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Full-screen [content] (an empty or error message) that still supports pull-to-refresh.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PullToRefreshStatus(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        // PullToRefreshBox only reacts to nested scroll, so the content must be scrollable.
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    content()
                }
            }
        }
    }
}
