package documentation

import documentation.model.Component
import documentation.model.Component.Relationship.CLOSE
import documentation.model.Component.Relationship.EXTERNAL
import documentation.model.Component.Relationship.OWNED
import documentation.model.Component.Type.BACKEND
import documentation.model.Component.Type.DATABASE
import documentation.model.Component.Type.FRONTEND
import documentation.model.Dependent
import documentation.model.DiagramComponent
import documentation.model.DiagramRelationship
import documentation.model.RootComponent

class DetailedDiagramGenerator(private val rootComponent: RootComponent) {

    private val components: List<Component>
    private val incomingRelationships: List<DiagramRelationship>
    private val outgoingRelationships: List<DiagramRelationship>

    private val systemAndContextMap: Map<SystemAndContextId, List<String>>

    init {
        components = rootComponent.dependents + rootComponent + rootComponent.dependencies

        incomingRelationships = rootComponent.dependents
            .map { source -> diagramRelationship(source, rootComponent) }
        outgoingRelationships = rootComponent.dependencies
            .map { target -> diagramRelationship(rootComponent, target) }

        systemAndContextMap = components
            .groupBy(::systemAndContextId, ::diagramComponentId)
    }

    fun generate(): String =
        buildString {
            appendLine("@startuml")
            appendLine("'https://plantuml.com/deployment-diagram")
            appendLine()
            systemAndContextMap.keys.forEach { (systemId, contextId) ->
                if (systemId != null) appendLine("""folder "${systemName(systemId)}" {""")
                if (contextId != null) appendLine("""frame "${contextName(contextId)}" {""")

                components
                    .filter { it.systemId == systemId && it.contextId == contextId }
                    .map(::diagramComponent)
                    .forEach { appendLine(it) }

                if (contextId != null) appendLine("}")
                if (systemId != null) appendLine("}")
            }
            appendLine()
            incomingRelationships.forEach { appendLine(it) }
            outgoingRelationships.forEach { appendLine(it) }
            appendLine()
            appendLine("@enduml")
        }

    // common functions

    private fun StringBuilder.appendLine(component: DiagramComponent) =
        with(component) {
            appendLine("""$type "$name" as $id $style""")
        }

    private fun StringBuilder.appendLine(relationship: DiagramRelationship) =
        with(relationship) {
            appendLine("""$source $link $target""")
        }

    private fun diagramComponent(description: Component) =
        DiagramComponent(
            id = diagramComponentId(description),
            type = type(description),
            name = name(description),
            style = style(description)
        )

    private fun systemAndContextId(it: Component): SystemAndContextId =
        SystemAndContextId(it.systemId, it.contextId)

    private fun diagramComponentId(component: Component): String =
        component.id.replace('-', '_').lowercase()

    private fun type(component: Component): String {
        return when (component.type) {
            BACKEND, FRONTEND -> "rectangle"
            DATABASE -> "database"
            else -> "circle"
        }
    }

    private fun name(component: Component): String =
        componentName(component.id)

    private fun style(component: Component): String {
        if (component is RootComponent) return "#skyblue;line.bold"
        if (component is Dependent) return "#lightgrey"
        if (component.type == DATABASE) return ""

        return when (component.relationship) {
            OWNED -> "#lightblue"
            CLOSE -> "#moccasin"
            EXTERNAL -> "#lightcoral"
            else -> ""
        }
    }

    private fun diagramRelationship(source: Component, target: Component): DiagramRelationship =
        DiagramRelationship(
            source = diagramComponentId(source),
            target = diagramComponentId(target),
            link = when (target.type) {
                DATABASE -> "-d->"
                else -> when (target.relationship) {
                    OWNED -> "-r->"
                    CLOSE -> "-d->"
                    else -> "-->"
                }
            }
        )

    data class SystemAndContextId(val systemId: String?, val contextId: String?)
}
