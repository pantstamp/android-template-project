package com.pantelisstampoulis.androidtemplateproject.domain

import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.error.ErrorModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.CoroutineContext

/**
 * A sealed class representing the possible states of a result in a data operation.
 *
 * This class is used to encapsulate the state of an operation that can result in a successful
 * data retrieval,a loading state indicating an ongoing process, or an error state when an
 * operation fails. It provides a type-safe way to manage and handle these different outcomes,
 * especially in asynchronous operations such as network calls or database queries.
 *
 * Repositories and use cases should return instances of this class to encapsulate the outcomes of
 * their operations.This ensures a consistent and unified approach to handling results and errors
 * throughout the application.
 *
 * @param T The type of data returned in the success state.
 */
sealed class ResultState<out T> {

    data class Success<T>(
        val data: T,
    ) : ResultState<T>()

    data object Loading : ResultState<Nothing>()

    data class Error(
        val error: ErrorModel,
    ) : ResultState<Nothing>()
}

fun <T> Flow<ResultState<T>>.onStartCatch(
    coroutineContext: CoroutineContext,
    logger: Logger,
) = this
    .onStart {
        emit(value = ResultState.Loading)
    }
    .catch { throwable ->
        logger.e(throwable = throwable) {
            "Exception caught in use case"
        }
        emit(value = ResultState.Error(ErrorModel.Unknown(throwable.message)))
    }
    .cancellable()
    .flowOn(context = coroutineContext)




/**
 * Extension function for [ResultState] to handle the success state.
 *
 * This function is invoked when the [ResultState] is of type [ResultState.Success].
 * It takes an action which is a lambda function that accepts an optional parameter of type [T].
 * The action is invoked with the data from the [ResultState.Success] state.
 *
 * @param action A lambda function to be invoked when the [ResultState] is [ResultState.Success].
 * @return The original [ResultState] after performing the action if it's a [ResultState.Success],
 * otherwise returns the original [ResultState] without any changes.
 */
inline fun <T> ResultState<T>.onSuccess(action: (T) -> Unit): ResultState<T> = when (this) {
    is ResultState.Success -> apply { action(data) }
    else -> this
}

/**
 * Extension function for [ResultState] to handle the loading state.
 *
 * This function is invoked when the [ResultState] is of type [ResultState.Loading].
 * It takes an action which is a lambda function that doesn't accept any parameters.
 * The action is invoked when the [ResultState] is [ResultState.Loading].
 *
 * @param action A lambda function to be invoked when the [ResultState] is [ResultState.Loading].
 * @return The original [ResultState] after performing the action if it's a [ResultState.Loading],
 * otherwise returns the original [ResultState] without any changes.
 */
inline fun <T> ResultState<T>.onLoading(action: () -> Unit): ResultState<T> = when (this) {
    is ResultState.Loading -> apply { action() }
    else -> this
}

/**
 * Extension function for [ResultState] to handle the error state.
 *
 * This function is invoked when the [ResultState] is of type [ResultState.Error].
 * It takes an action which is a lambda function that accepts a parameter of type [Throwable].
 * The action is invoked with the throwable from the [ResultState.Error] state.
 *
 * @param action A lambda function to be invoked when the [ResultState] is [ResultState.Error].
 * @return The original [ResultState] after performing the action if it's a [ResultState.Error],
 * otherwise returns the original [ResultState] without any changes.
 */
inline fun <T> ResultState<T>.onError(action: (ErrorModel) -> Unit): ResultState<T> = when (this) {
    is ResultState.Error -> apply { action(this.error) }
    else -> this
}
