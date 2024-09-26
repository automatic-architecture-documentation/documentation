package documentation.generators.plantuml

import documentation.generators.plantuml.DiagramDirection.TOP_TO_BOTTOM
import documentation.model.ApplicationComponent
import documentation.model.ComponentType
import documentation.model.ComponentType.BACKEND
import documentation.model.ComponentType.FRONTEND

class MultipleApplicationsDiagramGenerator(
    private val applicationComponents: List<ApplicationComponent>,
    private val options: Options = Options(),
) : AbstractDiagramGenerator(options) {

    data class Options(
        val direction: DiagramDirection = TOP_TO_BOTTOM,
        override val lineType: LineType = LineType.DEFAULT,
        override val includedComponentTypes: Set<ComponentType> = setOf(BACKEND, FRONTEND),
        override val includeCredentials: Boolean = false,
    ) : DiagramGeneratorOptions

    private val components: List<DiagramComponent>
    private val relationships: List<DiagramRelationship>

    init {
        val applicationComponentIds = applicationComponents.map(ApplicationComponent::id).toSet()

        components = buildList {
            applicationComponents
                .map(::diagramComponent)
                .forEach(::add)
            applicationComponents.asSequence()
                .flatMap { it.dependencies }
                .filter { it.type in options.includedComponentTypes }
                .filter { it.id !in applicationComponentIds }
                .distinctBy { it.id }
                .map(::diagramComponent)
                .forEach(::add)
        }
        relationships = applicationComponents
            .filter { it.type in options.includedComponentTypes }
            .flatMap { application ->
                application.dependencies
                    .filter { it.type in options.includedComponentTypes }
                    .map { dependency -> application to dependency }
            }
            .map { (source, target) -> diagramRelationship(source, target) }
    }

    override fun plantUmlSource(): String =
        buildString {
            appendLine("@startuml")
            appendLine("'https://plantuml.com/deployment-diagram")
            appendLine()
            appendLine("scale 1")
            appendLine()
            appendLine(options.direction)
            appendLine()
            appendLine(options.lineType)
            appendLine()
            components.forEach { appendComponentLine(it) }
            appendLine()
            relationships.forEach { appendRelationshipLine(it) }
            appendLine()
            appendLegend(applicationComponents)
            appendLine()
            appendLine("@enduml")
        }
}
