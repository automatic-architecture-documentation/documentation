package documentation.generators.plantuml

data class DiagramComponent(
    val id: String,
    val type: String,
    val name: String,
    val style: String?,
)

data class DiagramRelationship(
    val source: String,
    val target: String,
    val link: String,
    val label: String?,
)

data class DiagramNote(
    val target: String,
    val text: String,
    val position: String = "right",
)

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
