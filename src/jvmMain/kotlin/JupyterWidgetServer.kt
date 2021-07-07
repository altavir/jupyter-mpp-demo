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
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import org.jetbrains.kotlinx.jupyter.api.annotations.JupyterLibrary
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources

@JupyterLibrary
internal class JupyterWidgetServer : JupyterIntegration() {

    private val server: ApplicationEngine = embeddedServer(CIO, port = 9999) {
        //Server-bound widget container
        val widgets = WidgetContainer(this)

        install(WebSockets)
        install(CORS) {
            anyHost()
        }

        routing {
            route("/widgets") {
                get("state") {
                    call.respondText(widgets.getState().toString(), ContentType.Application.Json)
                }
                webSocket("updates") {
                    val incomingJob = incoming.consumeAsFlow().onEach {
                        val json = Json.parseToJsonElement(it.data.decodeToString())
                        (json as? JsonObject)?.forEach { (name, state) ->
                            widgets.update(name, state)
                        }
                    }.launchIn(this)

                    val outgoingJob = widgets.changes.onEach { (name, state) ->
                        //TODO add aggregator
                        val json = buildJsonObject {
                            put(name, state)
                        }
                        outgoing.send(Frame.Text(Json.encodeToString(JsonObject.serializer(), json)))
                    }
                }
            }
        }
    }

    private fun widget(widget: Widget): MimeTypedResult = HTML(createHTML().apply {
        widget(widget)
    }.finalize())

    override fun Builder.onLoaded() {

        resources {
            js("jupyter-mpp"){
                classPath("js/jupyter-mpp.js")
            }
        }

        onLoaded { server.start() }
        onShutdown { server.stop(1000, 1000) }

        import<TextWidget>()

        render<TextWidget> { textWidget ->
            widget(textWidget)
        }
    }
}