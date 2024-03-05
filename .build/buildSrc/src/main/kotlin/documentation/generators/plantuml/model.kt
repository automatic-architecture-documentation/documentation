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
    val link: String
)

data class DiagramNote(
    val target: String,
    val text: String,
    val position: String = "right",
)

enum class DiagramDirection {
    TOP_TO_BOTTOM,
    LEFT_TO_RIGHT
}
