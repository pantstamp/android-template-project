package com.pantelisstampoulis.androidtemplateproject.presentation.mvi

import com.pantelisstampoulis.androidtemplateproject.presentation.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<E : Event, US : UiState, SE : SideEffect>(
    initialState: US,
    scope: CoroutineScope? = null,
) : ViewModel(scope = scope) {

    private val _viewState: MutableStateFlow<US> = MutableStateFlow(initialState)
    val viewState: StateFlow<US> = _viewState.asStateFlow()

    private val eventFlow: MutableSharedFlow<E> = MutableSharedFlow()

    private val _effect: Channel<SE> = Channel()
    val effect = _effect.receiveAsFlow()

    init {
        subscribeToEvents()
    }

    fun setEvent(event: E) {
        viewModelScope.launch {
            eventFlow.emit(event)
        }
    }

    protected fun setState(reducer: US.() -> US) {
        _viewState.update { viewState.value.reducer() }
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            eventFlow.collect { event ->
                handleEvents(event)
            }
        }
    }

    abstract fun handleEvents(event: E)

    protected fun setEffect(builder: () -> SE) {
        viewModelScope.launch {
            val effectValue = builder()
            _effect.send(effectValue)
        }
    }
}
