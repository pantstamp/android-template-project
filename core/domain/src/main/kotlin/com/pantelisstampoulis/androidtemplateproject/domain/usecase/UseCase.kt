package com.pantelisstampoulis.androidtemplateproject.domain.usecase

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import kotlinx.coroutines.flow.Flow

interface UseCase<I, M> {

    operator fun invoke(
        input: I,
    ): Flow<ResultState<M>>
}
