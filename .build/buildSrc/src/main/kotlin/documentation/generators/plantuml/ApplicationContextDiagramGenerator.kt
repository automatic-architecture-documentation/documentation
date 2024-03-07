package documentation.generators.plantuml

import documentation.generators.groupName
import documentation.generators.plantuml.DiagramDirection.LEFT_TO_RIGHT
import documentation.generators.systemName
import documentation.model.*
import documentation.model.ComponentType.DATABASE
import documentation.model.Distance.OWNED
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class ApplicationContextDiagramGenerator(
    private val application: Application,
    private val options: Options = Options(),
) : AbstractDiagramGenerator() {

    data class Options(
        val includedComponentTypes: Set<ComponentType> = ComponentType.entries.toSet(),
        val includeSystemBoundaries: Boolean = false,
        val includeGroupBoundaries: Boolean = false,
        val includeHttpEndpointsNotes: Boolean = false,
        val lineType: LineType = LineType.DEFAULT,
    )

    // DATA PREPARATION

    private val components: List<Component>
    private val relationships: List<DiagramRelationship>
    private val notes: List<DiagramNote>
    private val systemsAndGroups: List<Pair<String?, String?>>

    init {
        val dependents = application.dependents.filter(::typeIsIncluded)
        val dependencies = application.dependencies.filter(::typeIsIncluded)

        components = buildList {
            addAll(dependents)
            add(application)
            addAll(dependencies)
        }

        relationships = buildList {
            dependents
                .map { source -> diagramRelationship(source, application) }
                .forEach(::add)
            dependencies
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

    private fun typeIsIncluded(component: Component): Boolean =
        component.type in options.includedComponentTypes

    // RENDERING

    override fun plantUmlSource(): String =
        buildString {
            appendLine("@startuml")
            appendLine("'https://plantuml.com/deployment-diagram")
            appendLine()
            appendLine(LEFT_TO_RIGHT)
            appendLine()
            appendLine(options.lineType)
            appendLine()
            systemsAndGroups
                .forEach { (systemId, groupId) ->
                    if (renderSystemBoundary(systemId)) appendLine("""folder "${systemName(systemId)}" {""")
                    if (renderGroupBoundary(groupId)) appendLine("""frame "${groupName(groupId)}" {""")

                    val componentsOfThisGroup = components
                        .filter { it.systemId == systemId && it.groupId == groupId }
                        .map(::diagramComponent)
                    val componentIdsOfThisGroup = componentsOfThisGroup
                        .map(DiagramComponent::id)
                        .toSet()
                    val notesOfThisGroup = notes
                        .filter { it.target in componentIdsOfThisGroup }

                    componentsOfThisGroup.forEach { appendComponentLine(it) }
                    notesOfThisGroup.forEach { appendNote(it) }

                    if (renderGroupBoundary(groupId)) appendLine("}")
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

    private fun StringBuilder.appendNote(note: DiagramNote) =
        with(note) {
            appendLine()
            appendLine("note $position of $target")
            appendLine(text)
            appendLine("end note")
            appendLine()
        }

    // RENDERING DECISIONS

    private fun httpEndpointNotes(components: List<Component>): List<DiagramNote> =
        if (options.includeHttpEndpointsNotes) {
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
            else -> when (options.includeGroupBoundaries) {
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
        return options.includeSystemBoundaries && systemId != null
    }

    private fun renderGroupBoundary(groupId: String?): Boolean {
        contract {
            returns(true) implies (groupId != null)
        }
        return options.includeGroupBoundaries && groupId != null
    }
}
