package documentation.generators.plantuml

import documentation.generators.groupName
import documentation.generators.plantuml.DiagramDirection.LEFT_TO_RIGHT
import documentation.generators.systemName
import documentation.model.Application
import documentation.model.Component
import documentation.model.ComponentType.DATABASE
import documentation.model.Dependency
import documentation.model.Dependent
import documentation.model.Distance.OWNED
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class ApplicationContextDiagramGenerator(
    private val application: Application,
    private val includeSystemBoundaries: Boolean,
    private val includeGroupBoundaries: Boolean,
    private val includeHttpEndpointsNotes: Boolean,
    private val lineType: LineType = LineType.DEFAULT,
) : AbstractDiagramGenerator() {

    // DATA PREPARATION

    private val components: List<Component>
    private val relationships: List<DiagramRelationship>
    private val notes: List<DiagramNote>
    private val systemsAndGroups: List<Pair<String?, String?>>

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

        notes = buildList {
            addAll(httpEndpointNotes(components))
        }

        systemsAndGroups = components
            .map { component -> component.systemId to component.groupId }
            .distinct()
    }

    // RENDERING

    override fun plantUmlSource(): String =
        buildString {
            appendLine("@startuml")
            appendLine("'https://plantuml.com/deployment-diagram")
            appendLine()
            appendLine(LEFT_TO_RIGHT)
            appendLine()
            appendLine(lineType)
            appendLine()
            systemsAndGroups
                .forEach { (systemId, groupId) ->
                    if (renderSystemBoundary(systemId)) appendLine("""folder "${systemName(systemId)}" {""")
                    if (renderGroupBoundary(groupId)) appendLine("""frame "${groupName(groupId)}" {""")

                    components
                        .filter { it.systemId == systemId && it.groupId == groupId }
                        .map(::diagramComponent)
                        .forEach { appendComponentLine(it) }

                    if (renderGroupBoundary(groupId)) appendLine("}")
                    if (renderSystemBoundary(systemId)) appendLine("}")
                }
            appendLine()
            relationships
                .forEach { relationship ->
                    appendRelationshipLine(relationship)
                }
            appendLine()
            notes
                .forEach { note ->
                    appendNote(note)
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

    private fun StringBuilder.appendNote(note: DiagramNote) =
        with(note) {
            appendLine("note $position of $target")
            appendLine(text)
            appendLine("end note")
        }

    // RENDERING DECISIONS

    private fun httpEndpointNotes(components: List<Component>): List<DiagramNote> =
        if (includeHttpEndpointsNotes) {
            components.filterIsInstance<Dependency>()
                .filter { it.httpEndpoints.isNotEmpty() }
                .map { dependency -> httpEndpointNote(dependency) }
        } else {
            emptyList()
        }

    private fun httpEndpointNote(dependency: Dependency) =
        DiagramNote(
            target = diagramComponentId(dependency),
            text = dependency.httpEndpoints
                .joinToString(prefix = "HTTP Endpoints:\n", separator = "\n") { endpoint ->
                    "${endpoint.method} ${endpoint.path}"
                }
        )

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
            else -> when (includeGroupBoundaries) {
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

    private fun renderGroupBoundary(groupId: String?): Boolean {
        contract {
            returns(true) implies (groupId != null)
        }
        return includeGroupBoundaries && groupId != null
    }
}
