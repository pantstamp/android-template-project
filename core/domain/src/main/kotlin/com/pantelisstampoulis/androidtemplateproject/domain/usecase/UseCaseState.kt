package com.pantelisstampoulis.androidtemplateproject.domain.usecase

import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.CoroutineContext

sealed class UseCaseState<out T> {

    data class Success<T>(
        val data: T,
    ) : UseCaseState<T>()

    data object Loading : UseCaseState<Nothing>()

    data class Error(
        val throwable: Throwable,
    ) : UseCaseState<Nothing>()
}

fun <T> Flow<UseCaseState<T>>.onStartCatch(
    coroutineContext: CoroutineContext,
    logger: Logger,
) = this
    .onStart {
        emit(value = UseCaseState.Loading)
    }
    .catch { throwable ->
        logger.e(throwable = throwable) {
            "Exception caught in use case"
        }
        emit(value = UseCaseState.Error(throwable = throwable))
    }
    .cancellable()
    .flowOn(context = coroutineContext)

/**
 * Extension function for [UseCaseState] to handle the success state.
 *
 * This function is invoked when the [UseCaseState] is of type [UseCaseState.Success].
 * It takes an action which is a lambda function that accepts an optional parameter of type [T].
 * The action is invoked with the data from the [UseCaseState.Success] state.
 *
 * @param action A lambda function to be invoked when the [UseCaseState] is [UseCaseState.Success].
 * @return The original [UseCaseState] after performing the action if it's a [UseCaseState.Success],
 * otherwise returns the original [UseCaseState] without any changes.
 */
inline fun <T> UseCaseState<T>.onSuccess(action: (T) -> Unit): UseCaseState<T> = when (this) {
    is UseCaseState.Success -> apply { action(data) }
    else -> this
}

/**
 * Extension function for [UseCaseState] to handle the loading state.
 *
 * This function is invoked when the [UseCaseState] is of type [UseCaseState.Loading].
 * It takes an action which is a lambda function that doesn't accept any parameters.
 * The action is invoked when the [UseCaseState] is [UseCaseState.Loading].
 *
 * @param action A lambda function to be invoked when the [UseCaseState] is [UseCaseState.Loading].
 * @return The original [UseCaseState] after performing the action if it's a [UseCaseState.Loading],
 * otherwise returns the original [UseCaseState] without any changes.
 */
inline fun <T> UseCaseState<T>.onLoading(action: () -> Unit): UseCaseState<T> = when (this) {
    is UseCaseState.Loading -> apply { action() }
    else -> this
}

/**
 * Extension function for [UseCaseState] to handle the error state.
 *
 * This function is invoked when the [UseCaseState] is of type [UseCaseState.Error].
 * It takes an action which is a lambda function that accepts a parameter of type [Throwable].
 * The action is invoked with the throwable from the [UseCaseState.Error] state.
 *
 * @param action A lambda function to be invoked when the [UseCaseState] is [UseCaseState.Error].
 * @return The original [UseCaseState] after performing the action if it's a [UseCaseState.Error],
 * otherwise returns the original [UseCaseState] without any changes.
 */
inline fun <T> UseCaseState<T>.onError(action: (Throwable) -> Unit): UseCaseState<T> = when (this) {
    is UseCaseState.Error -> apply { action(throwable) }
    else -> this
}
