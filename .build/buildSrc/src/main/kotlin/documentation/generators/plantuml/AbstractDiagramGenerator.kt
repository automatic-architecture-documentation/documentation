package documentation.generators.plantuml

import documentation.generators.componentName
import documentation.model.Component

abstract class AbstractDiagramGenerator : DiagramGenerator {

    private val invalidIdCharacters = Regex("[^0-9a-z_]")

    protected fun diagramDirection(direction: DiagramDirection): String =
        when(direction) {
            DiagramDirection.TOP_TO_BOTTOM -> "top to bottom direction"
            DiagramDirection.LEFT_TO_RIGHT -> "left to right direction"
        }

    protected fun diagramComponent(component: Component) =
        DiagramComponent(
            id = diagramComponentId(component),
            type = type(component),
            name = componentName(component.id),
            style = style(component)
        )

    private fun diagramComponentId(component: Component): String =
        listOfNotNull(component.systemId, component.contextId, component.id)
            .joinToString(separator = "__", transform = ::normalizeIdPart)

    private fun normalizeIdPart(value: String): String =
        value.lowercase()
            .replace('-', '_')
            .replace(invalidIdCharacters, "")

    private fun type(component: Component): String =
        when (component.type) {
            Component.Type.BACKEND, Component.Type.FRONTEND -> "rectangle"
            Component.Type.DATABASE -> "database"
            null -> "circle"
        }

    protected abstract fun style(component: Component): String

    protected fun defaultStyle(component: Component) =
        when (component.distanceFromUs) {
            Component.Distance.OWNED -> "#lightblue"
            Component.Distance.CLOSE -> "#moccasin"
            Component.Distance.DISTANT -> "#lightcoral"
            null -> ""
        }

    protected fun diagramRelationship(source: Component, target: Component): DiagramRelationship =
        DiagramRelationship(
            source = diagramComponentId(source),
            target = diagramComponentId(target),
            link = link(target)
        )

    protected abstract fun link(target: Component): String
}
