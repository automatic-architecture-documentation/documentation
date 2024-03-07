package documentation.generators.plantuml

import documentation.generators.plantuml.DiagramDirection.TOP_TO_BOTTOM
import documentation.model.Application
import documentation.model.Component
import documentation.model.ComponentType
import documentation.model.ComponentType.BACKEND
import documentation.model.ComponentType.FRONTEND

class MultipleApplicationsDiagramGenerator(
    private val applications: List<Application>,
    private val options: Options = Options(),
) : AbstractDiagramGenerator() {

    data class Options(
        val direction: DiagramDirection = TOP_TO_BOTTOM,
        val includedComponentTypes: Set<ComponentType> = setOf(BACKEND, FRONTEND),
        val lineType: LineType = LineType.DEFAULT,
    )

    // DATA PREPARATION

    private val components: List<DiagramComponent>
    private val relationships: List<DiagramRelationship>

    init {
        val applicationIds = applications.map(Application::id).toSet()

        components = buildList {
            applications
                .map(::diagramComponent)
                .forEach(::add)
            applications.asSequence()
                .flatMap { it.dependencies }
                .filter { it.type in options.includedComponentTypes }
                .filter { it.id !in applicationIds }
                .distinctBy { it.id }
                .map(::diagramComponent)
                .forEach(::add)
        }
        relationships = applications
            .filter { it.type in options.includedComponentTypes }
            .flatMap { application ->
                application.dependencies
                    .filter { it.type in options.includedComponentTypes }
                    .map { dependency -> application to dependency }
            }
            .map { (source, target) -> diagramRelationship(source, target) }
    }

    // RENDERING

    override fun plantUmlSource(): String =
        buildString {
            appendLine("@startuml")
            appendLine("'https://plantuml.com/deployment-diagram")
            appendLine()
            appendLine(options.direction)
            appendLine()
            appendLine(options.lineType)
            appendLine()
            components.forEach { appendComponentLine(it) }
            appendLine()
            relationships.forEach { appendRelationshipLine(it) }
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

    override fun style(component: Component) = defaultStyle(component)
    override fun link(target: Component) = "-->"
}
