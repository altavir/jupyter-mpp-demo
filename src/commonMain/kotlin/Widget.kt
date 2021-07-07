import Widget.Companion.WIDGET_DATA_CLASS
import Widget.Companion.WIDGET_ID_ATTRIBUTE
import Widget.Companion.WIDGET_TAG_CLASS
import Widget.Companion.WIDGET_TYPE_ATTRIBUTE
import kotlinx.coroutines.flow.Flow
import kotlinx.html.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

typealias WidgetState = JsonElement


interface Widget {
    /**
     * The type of a widget
     */
    val type: String

    var value: WidgetState

    /**
     * Current state and its changes
     */
    val changes: Flow<WidgetState>

    companion object {
        const val WIDGET_TAG_CLASS = "kotlin-widget"
        const val WIDGET_DATA_CLASS = "kotlin-widget-data"
        const val WIDGET_ID_ATTRIBUTE = "data-widget-id"
        const val WIDGET_TYPE_ATTRIBUTE = "data-widget-type"
    }
}

private var counter = 0

internal fun <R> TagConsumer<R>.widget(name: String, widget: Widget): R = div {
    classes = classes + WIDGET_TAG_CLASS
    val widgetId = "kotlin-widget-${counter++}"
    id = widgetId
    attributes[WIDGET_ID_ATTRIBUTE] = name
    attributes[WIDGET_TYPE_ATTRIBUTE] = widget.type
    //encode initial state
    script {
        type = "text/json"
        attributes["class"] = WIDGET_DATA_CLASS
        unsafe {
            +Json.encodeToString(JsonElement.serializer(), widget.value)
        }
    }
    script {
        type = "text/javascript"
        unsafe {
            //language=JavaScript
            +"""
                renderWidget("$widgetId");
            """.trimIndent()
        }
    }
}
