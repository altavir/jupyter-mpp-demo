import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.w3c.dom.WebSocket
import kotlin.coroutines.EmptyCoroutineContext

//TODO move to a separate project

val container by lazy { WidgetContainer(CoroutineScope(EmptyCoroutineContext)) }

val webSocket by lazy {
    WebSocket("ws://localhost:9999/widgets/updates").apply {
        onmessage = { messageEvent ->
            val stringData: String? = messageEvent.data as? String
            if (stringData != null) {
                val json = Json.parseToJsonElement(stringData)
                (json as? JsonObject)?.forEach { (name, state) ->
                    container.update(name, state)
                }
            }
        }
        onopen = {
            console.info("WebSocket update channel established")
        }
        onclose = {
            console.info("WebSocket update channel closed")
        }
        onerror = {
            console.error("WebSocket update channel error")
        }
    }
}

fun main() {

    val renderers = listOf(
        TextInputWidgetRenderer
    )

    val renderFunction: (String) -> Unit = { id ->
        //initialize
        webSocket
        val element = document.getElementById(id) ?: error("Element with id $id not found")
        container.renderWidgetsAt(element, renderers)
    }

    window.asDynamic()["renderWidget"] = renderFunction
}