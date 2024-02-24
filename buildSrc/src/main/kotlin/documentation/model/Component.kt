package documentation.model

sealed interface Component {

    val id: String
    val contextId: String?
    val systemId: String?
    val type: Type?
    val relationship: Relationship?

    enum class Type {
        BACKEND, FRONTEND, DATABASE
    }

    enum class Relationship {
        OWNED, CLOSE, EXTERNAL
    }
}
