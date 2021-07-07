import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import org.slf4j.LoggerFactory

@JupyterLibrary
internal class JupyterWidgetServer : JupyterIntegration() {

    private val scope = CoroutineScope(Dispatchers.Default)

    //Server-bound widget container
    val widgetContainer = WidgetContainer(scope)

    private val server: ApplicationEngine = scope.embeddedServer(CIO, port = 9999) {

        val logger = LoggerFactory.getLogger("Widgets")

        install(WebSockets)
        install(CORS) {
            anyHost()
        }

        routing {
            route("/widgets") {
                get("state") {
                    call.respondText(widgetContainer.getState().toString(), ContentType.Application.Json)
                }
                webSocket("updates") {
                    logger.info("Websocket connection received")
                    launch {
                        for (frame in incoming) {
                            val json = Json.parseToJsonElement(frame.data.decodeToString())
                            logger.debug("RECEIVED: $json")
                            (json as? JsonObject)?.forEach { (name, state) ->
                                widgetContainer.update(name, state)
                            }
                        }
                    }

                    //suspend reading all changes
                    widgetContainer.changes.collect { (name, state) ->
                        //TODO add aggregator
                        val json = buildJsonObject {
                            put(name, state)
                        }
                        outgoing.send(Frame.Text(Json.encodeToString(JsonObject.serializer(), json)))
                        logger.debug("SENT: $json")
                    }
                }
            }
        }
    }

    private fun widgetHtml(widget: Widget): MimeTypedResult = HTML(createHTML().apply {
        val name = widget.toString()
        widgetContainer.registerWidget(name, widget)
        widget(name, widget)
    }.finalize())

    override fun Builder.onLoaded() {

        resources {
            js("jupyter-mpp") {
                classPath("js/jupyter-mpp.js")
            }
        }

        onLoaded { server.start() }
        onShutdown { server.stop(1000, 1000) }

        import<TextWidget>()
        import<CheckBox>()

        render<Widget> { widget ->
            widgetHtml(widget)
        }
    }
}