import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

/**
 * A generic widget without additional state
 */
open class GenericWidget(
    override val type: String,
    initialState: WidgetState?,
) : Widget {
    protected val _state = MutableStateFlow<WidgetState>(initialState ?: JsonObject(emptyMap()))

    override var value: WidgetState
        get() = _state.value
        set(value) { _state.value = value}


    override val changes: StateFlow<WidgetState>
        get() = _state
}

