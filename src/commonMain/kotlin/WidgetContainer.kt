import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class WidgetContainer(val scope: CoroutineScope) {
    private val widgets = HashMap<String, Pair<Widget, Job>>()

    private val sharedFlow = MutableSharedFlow<Pair<String, WidgetState>>()
    public val changes: SharedFlow<Pair<String, WidgetState>> = sharedFlow

    /**
     * Register a widget and subscribe on its updates
     */
    fun registerWidget(name: String, widget: Widget) {
        //Cancel existing subscription if needed
        widgets[name]?.second?.cancel()
        val readJob = widget.changes.onEach {
            sharedFlow.emit(name to it)
        }.launchIn(scope)
        widgets[name] = widget to readJob
    }

    fun update(name: String, state: WidgetState){
        scope.launch {
            widgets[name]?.first?.let { it.value = state }
        }
    }

    /**
     * Subscribe to external updates
     */
    fun subscribe(flow: Flow<Pair<String, WidgetState>>) {
        flow.onEach { (name, state) ->
            widgets[name]?.first?.let { it.value = state }
        }.launchIn(scope)
    }

    /**
     * Snapshot current state of widget container
     */
    suspend fun getState(): JsonObject = buildJsonObject {
        widgets.forEach { (name, pair) ->
            put(name, pair.first.value)
        }
    }

}