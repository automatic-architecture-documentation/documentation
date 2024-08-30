package documentation.generators.plantuml

import documentation.model.Component
import documentation.model.Dependency

data class DiagramComponent(
    val id: String,
    val type: String,
    val name: String,
    val style: String? = null,
)

data class DiagramRelationship(
    val source: String,
    val target: String,
    val link: String,
    val label: String? = null,
)

data class DiagramNote(
    val target: String,
    val text: String,
    val position: String = "right",
) {
    companion object {
        fun httpEndpoints(dependency: Dependency, qualifier: String? = null): DiagramNote {
            val methodLength = dependency.httpEndpoints.maxOfOrNull { it.method.length } ?: 0
            return DiagramNote(
                target = diagramComponentId(dependency, qualifier),
                text = dependency.httpEndpoints
                    .joinToString(prefix = "**HTTP Endpoints:**\n", separator = "\n") { endpoint ->
                        "\"\"${endpoint.method.padEnd(methodLength, ' ')} ${endpoint.path}\"\""
                    }
            )
        }
    }
}

enum class DiagramDirection(private val value: String) {
    TOP_TO_BOTTOM("top to bottom direction"),
    LEFT_TO_RIGHT("left to right direction");

    override fun toString() = value
}

enum class LineType(private val value: String) {
    DEFAULT(""),
    STRAIGHT("skinparam linetype line"),
    BEZIER("skinparam linetype bezier"),
    POLY("skinparam linetype polyline"),
    ORTHOGONAL("skinparam linetype ortho"), ;

    override fun toString() = value
}

// ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ###

private val invalidIdCharacters = Regex("[^0-9a-z_]")

fun diagramComponentId(component: Component, qualifier: String?): String =
    listOfNotNull(component.systemId, component.groupId, component.id, qualifier)
        .joinToString(separator = "__", transform = ::normalizeIdPart)

fun normalizeIdPart(value: String): String =
    value.lowercase()
        .replace('-', '_')
        .replace(invalidIdCharacters, "")
