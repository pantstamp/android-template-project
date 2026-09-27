package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.discover

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R

/**
 * The Discover search field. It owns no search logic: the text lives in [state], and
 * [DiscoverScreen] forwards every change to the search ViewModel.
 */
@Composable
fun MovieSearchField(state: TextFieldState, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        lineLimits = TextFieldLineLimits.SingleLine,
        placeholder = { Text(text = stringResource(id = R.string.search_movies_placeholder)) },
        leadingIcon = {
            Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = null)
        },
        trailingIcon = if (state.text.isNotEmpty()) {
            {
                // IconButton gives the 48 dp minimum touch target.
                IconButton(onClick = { state.clearText() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = stringResource(id = R.string.search_clear),
                    )
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // The search runs as the user types; the Search key only hides the keyboard.
        onKeyboardAction = { keyboardController?.hide() },
    )
}
