import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import org.w3c.dom.WebSocket
import kotlin.coroutines.EmptyCoroutineContext

//TODO move to a separate project

private val container by lazy { WidgetContainer(CoroutineScope(EmptyCoroutineContext)) }

private var webSocket: WebSocket? = null

fun getWebSocket(): WebSocket {
    if (webSocket == null) {
        var sendJob : Job? = null
        webSocket = WebSocket("ws://localhost:9999/widgets/updates").apply {
            onmessage = { messageEvent ->
                console.info("Websocket receive: $messageEvent")
                val stringData: String? = messageEvent.data as? String
                if (stringData != null) {
                    val json = Json.parseToJsonElement(stringData)
                    (json as? JsonObject)?.forEach { (name, state) ->
                        container.update(name, state)
                    }
                }
            }
            onopen = {
                sendJob = container.changes.onEach { (name, state) ->
                    //TODO add aggregator
                    val json = buildJsonObject {
                        put(name, state)
                    }
                    send(Json.encodeToString(JsonObject.serializer(), json))
                    console.info("Websocket send: $json")
                }.launchIn(container.scope)
                console.info("WebSocket update channel established")
            }
            onclose = {
                sendJob?.cancel()
                console.info("WebSocket update channel closed")
            }
            onerror = {
                console.error("WebSocket update channel error")
            }
        }
    }
    return webSocket!!
}

fun main() {

    val renderers = listOf(
        TextInputWidgetRenderer,
        CheckBoxWidgetRenderer
    )

    val renderFunction: (String) -> Unit = { id ->
        try {
            console.info("Rendering widget with id $id")
            getWebSocket()
            val element = document.getElementById(id) ?: error("Element with id $id not found")
            container.renderWidgetsAt(element, renderers)
        } catch (t: Throwable) {
            console.error(t)
        }
    }

    window.asDynamic()["renderWidget"] = renderFunction
}