package documentation.generators.plantuml

import documentation.generators.contextName
import documentation.generators.systemName
import documentation.model.Application
import documentation.model.Component
import documentation.model.Component.Distance.OWNED
import documentation.model.Component.Type.DATABASE
import documentation.model.Dependent
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class ApplicationContextDiagramGenerator(
    private val application: Application,
    private val includeSystemBoundaries: Boolean,
    private val includeContextBoundaries: Boolean,
) : AbstractDiagramGenerator() {

    // DATA PREPARATION

    private val components: List<Component>
    private val relationships: List<DiagramRelationship>
    private val systemsAndContexts: List<Pair<String?, String?>>

    init {
        components = buildList {
            addAll(application.dependents)
            add(application)
            addAll(application.dependencies)
        }

        relationships = buildList {
            application.dependents
                .map { source -> diagramRelationship(source, application) }
                .forEach(::add)
            application.dependencies
                .map { target -> diagramRelationship(application, target) }
                .forEach(::add)
        }

        systemsAndContexts = components
            .map { component -> component.systemId to component.contextId }
            .distinct()
    }

    // RENDERING

    override fun plantUmlSource(): String =
        buildString {
            appendLine("@startuml")
            appendLine("'https://plantuml.com/deployment-diagram")
            appendLine()
            appendLine("left to right direction")
            appendLine()
            systemsAndContexts
                .forEach { (systemId, contextId) ->
                    if (renderSystemBoundary(systemId)) appendLine("""folder "${systemName(systemId)}" {""")
                    if (renderContextBoundary(contextId)) appendLine("""frame "${contextName(contextId)}" {""")

                    components
                        .filter { it.systemId == systemId && it.contextId == contextId }
                        .map(::diagramComponent)
                        .forEach { appendComponentLine(it) }

                    if (renderContextBoundary(contextId)) appendLine("}")
                    if (renderSystemBoundary(systemId)) appendLine("}")
                }
            appendLine()
            relationships
                .forEach { relationship ->
                    appendRelationshipLine(relationship)
                }
            appendLine()
            appendLine("@enduml")
        }

    private fun StringBuilder.appendComponentLine(component: DiagramComponent) =
        with(component) {
            appendLine("""$type "$name" as $id $style""")
        }

    private fun StringBuilder.appendRelationshipLine(relationship: DiagramRelationship) =
        with(relationship) {
            appendLine("""$source $link $target""")
        }

    // RENDERING DECISIONS

    override fun style(component: Component): String =
        when {
            component is Application -> "#skyblue;line.bold"
            component is Dependent -> "#lightgrey"
            component.type == DATABASE -> ""
            else -> defaultStyle(component)
        }

    override fun link(target: Component): String =
        when (target.type) {
            DATABASE -> "-l->"
            else -> when (includeContextBoundaries) {
                true -> when (target.distanceFromUs) {
                    OWNED -> "-->"
                    else -> "--->"
                }

                false -> "-->"
            }
        }

    private fun renderSystemBoundary(systemId: String?): Boolean {
        contract {
            returns(true) implies (systemId != null)
        }
        return includeSystemBoundaries && systemId != null
    }

    private fun renderContextBoundary(contextId: String?): Boolean {
        contract {
            returns(true) implies (contextId != null)
        }
        return includeSystemBoundaries && contextId != null
    }
}
