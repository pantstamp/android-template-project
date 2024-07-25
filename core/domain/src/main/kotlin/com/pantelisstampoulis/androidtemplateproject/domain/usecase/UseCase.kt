package com.pantelisstampoulis.androidtemplateproject.domain.usecase

import kotlinx.coroutines.flow.Flow

interface UseCase<I, M> {

    operator fun invoke(
        input: I,
    ): Flow<UseCaseState<M>>
}
