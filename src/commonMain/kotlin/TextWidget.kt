import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class TextWidget(initialText: String = "") : Widget {
    override val type: String = "textField"

    private val _state = MutableStateFlow<String>(initialText)
    public val state: StateFlow<String> get() = _state

    override var value: WidgetState
        get() = JsonPrimitive(_state.value)
        set(value) {
            _state.value = value.jsonPrimitive.content
        }

    override val changes: Flow<WidgetState> get() = _state.map { JsonPrimitive(it) }

    public var text: String
        get() = _state.value
        set(value) {
            _state.value = value
        }

}