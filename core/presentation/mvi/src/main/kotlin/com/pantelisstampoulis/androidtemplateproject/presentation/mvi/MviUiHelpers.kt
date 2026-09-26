package com.pantelisstampoulis.androidtemplateproject.presentation.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Observes a [Flow] of effects and triggers the [onEffect] callback when an effect is emitted.
 * This function ensures that effects are observed only when the [LifecycleOwner] is in the STARTED state.
 *
 * Effects are collected on [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate],
 * so an effect is handled in the same frame it is sent, without being re-dispatched. The dispatcher is
 * not injected: tests control it through `Dispatchers.setMain`, and a caller that had to supply it would
 * need a DI container, which a Compose preview does not have.
 *
 * @param effect The [Flow] of effects to observe.
 * @param lifecycleOwner The [LifecycleOwner] whose lifecycle is used to control the observation.
 * Defaults to [LocalLifecycleOwner.current].
 * @param onEffect The callback to trigger when an effect is emitted.
 */
@Composable
fun <T> ObserveEffects(
    effect: Flow<T>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEffect: (T) -> Unit,
) {
    LaunchedEffect(effect, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                effect.collect(onEffect)
            }
        }
    }
}
