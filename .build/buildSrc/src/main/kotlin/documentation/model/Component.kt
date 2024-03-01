package documentation.model

sealed interface Component {

    val id: String
    val contextId: String?
    val systemId: String?
    val type: Type?
    val distanceFromUs: Distance?

    enum class Type {
        BACKEND, FRONTEND, DATABASE
    }

    enum class Distance {
        OWNED, CLOSE, DISTANT
    }
}
