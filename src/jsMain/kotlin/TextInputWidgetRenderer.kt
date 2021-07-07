import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.input
import kotlinx.html.js.onChangeFunction
import kotlinx.serialization.json.jsonPrimitive
import org.w3c.dom.Element

object TextInputWidgetRenderer : WidgetRenderer {
    override val type: String = "textField"

    override fun attach(scope: CoroutineScope, element: Element, initialData: WidgetState?): Widget {
        val widget = TextWidget(initialData?.jsonPrimitive?.content ?: "")
        element.append {
            input {
                type = InputType.text
                value = widget.text
                //Callback on text change
                onChangeFunction = {
                    widget.text = value
                }
                //Subscribe on updates
                widget.state.onEach {
                    value = it
                }.launchIn(scope)
            }
        }
        return widget
    }
}