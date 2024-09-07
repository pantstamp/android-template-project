package com.pantelisstampoulis.androidtemplateproject.presentation.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Observes a [Flow] of effects and triggers the [onEffect] callback when an effect is emitted.
 * This function ensures that effects are observed only when the [LifecycleOwner] is in the STARTED state.
 *
 * @param effect The [Flow] of effects to observe.
 * @param coroutineContext The [CoroutineContext] in which to collect the effects.
 * @param lifecycleOwner The [LifecycleOwner] whose lifecycle is used to control the observation.
 * @param onEffect The callback to trigger when an effect is emitted.
 */
@Composable
fun <T> ObserveEffects(
    effect: Flow<T>,
    coroutineContext: CoroutineContext,
    lifecycleOwner: LifecycleOwner,
    onEffect: (T) -> Unit,
) {
    LaunchedEffect(effect, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            withContext(coroutineContext) {
                effect.collect(onEffect)
            }
        }
    }
}
