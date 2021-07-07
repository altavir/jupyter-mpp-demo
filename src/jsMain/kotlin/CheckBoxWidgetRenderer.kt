import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.input
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.w3c.dom.Element

object CheckBoxWidgetRenderer : WidgetRenderer {
    override val type: String = "checkBox"

    override fun attach(scope: CoroutineScope, element: Element, initialData: WidgetState?): Widget {
        val widget = CheckBox(initialData?.jsonPrimitive?.booleanOrNull ?: false)
        element.append {
            input {
                type = InputType.checkBox
                checked = widget.checked
            }.apply {
                onchange = {
                    //Callback on text change
                    widget.checked = checked
                    Unit
                }
                //Subscribe on updates
                widget.state.onEach {
                    checked = it
                }.launchIn(scope)
            }
        }
        return widget
    }
}