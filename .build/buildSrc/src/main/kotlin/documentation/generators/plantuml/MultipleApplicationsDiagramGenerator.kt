package documentation.generators.plantuml

import documentation.model.Application
import documentation.model.Component
import documentation.model.Component.Type.BACKEND
import documentation.model.Component.Type.FRONTEND

class MultipleApplicationsDiagramGenerator(
    private val applications: List<Application>,
    private val direction: DiagramDirection,
) : AbstractDiagramGenerator() {

    private val relevantComponentTypes = setOf(BACKEND, FRONTEND)

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
                .filter { it.type in relevantComponentTypes }
                .filter { it.id !in applicationIds }
                .distinctBy { it.id }
                .map(::diagramComponent)
                .forEach(::add)
        }
        relationships = applications
            .filter { it.type in relevantComponentTypes }
            .flatMap { application ->
                application.dependencies
                    .filter { it.type in relevantComponentTypes }
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
            appendLine(diagramDirection(direction))
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