package documentation.generators.plantuml

import documentation.generators.groupName
import documentation.generators.systemName
import documentation.model.Application
import documentation.model.Component
import documentation.model.ComponentType.DATABASE
import documentation.model.Dependent
import documentation.model.Distance.OWNED
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class ApplicationContextDiagramGenerator(
    private val application: Application,
    private val includeSystemBoundaries: Boolean,
    private val includeGroupBoundaries: Boolean,
) : AbstractDiagramGenerator() {

    // DATA PREPARATION

    private val components: List<Component>
    private val relationships: List<DiagramRelationship>
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
            appendLine("left to right direction")
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
