package documentation.generators.plantuml

import documentation.generators.componentName
import documentation.model.Component
import documentation.model.ComponentType
import documentation.model.Dependency
import documentation.model.Distance

abstract class AbstractDiagramGenerator(
    private val options: DiagramGeneratorOptions
) : DiagramGenerator {

    interface DiagramGeneratorOptions {
        val lineType: LineType
        val includedComponentTypes: Set<ComponentType>
        val includeCredentials: Boolean
    }

    // RENDERING

    protected fun StringBuilder.appendComponentLine(component: DiagramComponent) =
        with(component) {
            appendLine("""$type "$name" as $id $style""")
        }

    protected fun StringBuilder.appendRelationshipLine(relationship: DiagramRelationship) =
        with(relationship) {
            appendLine("""$source $link $target ${label?.let { ":$it" } ?: ""}""")
        }

    protected fun StringBuilder.appendNote(note: DiagramNote) =
        with(note) {
            appendLine()
            appendLine("note $position of $target")
            appendLine(text)
            appendLine("end note")
            appendLine()
        }

    // CONVERTER

    protected fun diagramComponent(component: Component) =
        DiagramComponent(
            id = diagramComponentId(component),
            type = type(component),
            name = componentName(component.id),
            style = style(component)
        )

    protected fun diagramRelationship(source: Component, target: Component): DiagramRelationship =
        DiagramRelationship(
            source = diagramComponentId(source),
            target = diagramComponentId(target),
            link = link(target),
            label = linkLabel(source, target),
        )

    // RENDERING DECISIONS

    protected open fun type(component: Component): String =
        when (component.type) {
            ComponentType.BACKEND, ComponentType.FRONTEND -> "rectangle"
            ComponentType.DATABASE -> "database"
            null -> "circle"
        }

    protected open fun style(component: Component): String =
        when (component.distanceFromUs) {
            Distance.OWNED -> "#lightblue"
            Distance.CLOSE -> "#moccasin"
            Distance.DISTANT -> "#lightcoral"
            null -> ""
        }

    protected open fun link(target: Component): String = "-->"

    protected open fun linkLabel(source: Component, target: Component): String? =
        when (target) {
            is Dependency -> when {
                options.includeCredentials -> when (target.type) {
                    ComponentType.BACKEND -> credentialsLabel(target)
                    else -> null
                }

                else -> null
            }

            else -> null
        }

    private fun credentialsLabel(dependency: Dependency): String {
        if (dependency.credentials.isEmpty()) return "?"
        return dependency.credentials.joinToString(separator = ", ") { it.label }
    }
}
