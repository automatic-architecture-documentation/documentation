package documentation.generators.plantuml

import documentation.generators.componentName
import documentation.model.Component
import documentation.model.ComponentType
import documentation.model.Distance

abstract class AbstractDiagramGenerator : DiagramGenerator {

    private val invalidIdCharacters = Regex("[^0-9a-z_]")

    protected fun diagramComponent(component: Component) =
        DiagramComponent(
            id = diagramComponentId(component),
            type = type(component),
            name = componentName(component.id),
            style = style(component)
        )

    protected fun diagramComponentId(component: Component): String =
        listOfNotNull(component.systemId, component.groupId, component.id)
            .joinToString(separator = "__", transform = ::normalizeIdPart)

    private fun normalizeIdPart(value: String): String =
        value.lowercase()
            .replace('-', '_')
            .replace(invalidIdCharacters, "")

    private fun type(component: Component): String =
        when (component.type) {
            ComponentType.BACKEND, ComponentType.FRONTEND -> "rectangle"
            ComponentType.DATABASE -> "database"
            null -> "circle"
        }

    protected abstract fun style(component: Component): String

    protected fun defaultStyle(component: Component) =
        when (component.distanceFromUs) {
            Distance.OWNED -> "#lightblue"
            Distance.CLOSE -> "#moccasin"
            Distance.DISTANT -> "#lightcoral"
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
