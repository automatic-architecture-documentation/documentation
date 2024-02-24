package documentation

import documentation.model.Component
import documentation.model.Component.Relationship.CLOSE
import documentation.model.Component.Relationship.EXTERNAL
import documentation.model.Component.Relationship.OWNED
import documentation.model.Component.Type.BACKEND
import documentation.model.Component.Type.DATABASE
import documentation.model.Component.Type.FRONTEND
import documentation.model.DiagramComponent
import documentation.model.DiagramRelationship
import documentation.model.RootComponent

class OverviewDiagramGenerator(rootComponents: List<RootComponent>) {

    private val relevantTypes = setOf(BACKEND, FRONTEND)

    private val rootComponentIds = rootComponents.map { it.id }.toSet()
    private val diagramRootComponents = rootComponents
        .map { diagramComponent(it) }
    private val additionalDiagramComponents = rootComponents
        .asSequence()
        .flatMap { it.dependencies }
        .filter { it.id !in rootComponentIds }
        .filter { it.type in relevantTypes }
        .distinctBy { it.id }
        .map { diagramComponent(it) }
        .toList()
    private val diagramRelationships = rootComponents
        .flatMap { source ->
            source.dependencies
                .filter { it.type in relevantTypes }
                .map { target -> source to target }
        }
        .map { (source, target) -> diagramRelationship(source, target) }

    fun generate(): String =
        buildString {
            appendLine("@startuml")
            appendLine("'https://plantuml.com/deployment-diagram")
            appendLine()
            appendLine("left to right direction")
            appendLine()
            diagramRootComponents.forEach { appendLine(it) }
            additionalDiagramComponents.forEach { appendLine(it) }
            appendLine()
            diagramRelationships.forEach { appendLine(it) }
            appendLine()
            appendLine("@enduml")
        }

    // common functions

    private fun StringBuilder.appendLine(component: DiagramComponent) =
        with(component) {
            appendLine("""$type "$name" as $id ${if (style != null) "#$style" else ""}""")
        }

    private fun StringBuilder.appendLine(relationship: DiagramRelationship) =
        with(relationship) {
            appendLine("""$source $link $target""")
        }

    private fun diagramComponent(description: Component) =
        DiagramComponent(
            id = id(description),
            type = type(description),
            name = name(description),
            style = color(description)
        )

    private fun id(description: Component): String =
        description.id.replace('-', '_').lowercase()

    private fun type(description: Component): String {
        return when (description.type) {
            BACKEND, FRONTEND -> "rectangle"
            DATABASE -> "database"
            else -> "circle"
        }
    }

    private fun name(description: Component): String =
        description.id

    private fun color(description: Component): String? =
        when (description.type) {
            DATABASE -> null
            else -> when (description.relationship) {
                OWNED -> "lightblue"
                CLOSE -> "moccasin"
                EXTERNAL -> "lightcoral"
                else -> null
            }
        }

    private fun diagramRelationship(source: Component, target: Component): DiagramRelationship {
        val sourceComponent = diagramComponent(source)
        val targetComponent = diagramComponent(target)
        return DiagramRelationship(
            source = sourceComponent.id,
            target = targetComponent.id,
            link = "-->"
        )
    }
}
