import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

class CheckBox(initialValue: Boolean = false) : Widget {
    override val type: String = "checkBox"

    private val _state = MutableStateFlow<Boolean>(initialValue)
    public val state: StateFlow<Boolean> get() = _state

    override var value: WidgetState
        get() = JsonPrimitive(_state.value)
        set(value) {
            _state.value = value.jsonPrimitive.boolean
        }

    override val changes: Flow<WidgetState> get() = _state.map { JsonPrimitive(it) }

    var checked: Boolean
        get() = _state.value
        set(value) {
            _state.value = value
        }

}