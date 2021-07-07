import Widget.Companion.WIDGET_DATA_CLASS
import Widget.Companion.WIDGET_ID_ATTRIBUTE
import Widget.Companion.WIDGET_TAG_CLASS
import Widget.Companion.WIDGET_TYPE_ATTRIBUTE
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.get

interface WidgetRenderer {
    /**
     * The type of widget being rendered by this factory
     */
    val type: String

    fun attach(scope: CoroutineScope, element: Element, initialData: WidgetState?): Widget
}

/**
 * Render all widgets inside current element and register them in the container to receiver updates.
 */
internal fun WidgetContainer.renderWidgetsAt(parent: Element, renderers: Collection<WidgetRenderer>) {
    parent.getElementsByClassName(WIDGET_TAG_CLASS).asList().forEach { element ->
        val id = element.attributes[WIDGET_ID_ATTRIBUTE]?.value ?: "undefined"
        val type = element.attributes[WIDGET_TYPE_ATTRIBUTE]?.value ?: "undefined"
        val renderer = renderers.firstOrNull { it.type == type } ?: error("Renderer for widget type $type not found")
        val initialData = element.getElementsByClassName(WIDGET_DATA_CLASS).asList().firstOrNull()?.let {
            Json.parseToJsonElement(it.textContent ?: "{}")
        }
        val widget = renderer.attach(scope, element, initialData)
        registerWidget(id, widget)
    }
}