package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.error.LoadError

/**
 * A centred icon, message and optional Retry button, used for error and empty states. The
 * message is a polite live region, so screen readers announce it when it appears.
 */
@Composable
fun StatusMessage(message: String, @DrawableRes iconRes: Int?, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text(text = stringResource(id = R.string.action_retry))
            }
        }
    }
}

@DrawableRes
internal fun LoadError.iconRes(): Int = when (this) {
    LoadError.Offline -> R.drawable.ic_cloud_off
    LoadError.Generic -> R.drawable.ic_error
}
