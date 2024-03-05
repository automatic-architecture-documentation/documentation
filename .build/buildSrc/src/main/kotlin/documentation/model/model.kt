package documentation.model

// (!) The content of this file is copied from repository to repository.
// It is the model used by the various generation Gradle tasks and is the same for all repositories.
// In a real project this would likely be packaged as library package and distributed using some kind of registry.

sealed interface Component {
    val id: String
    val groupId: String?
    val systemId: String?
    val type: ComponentType?
    val distanceFromUs: Distance?
}

enum class ComponentType { BACKEND, FRONTEND, DATABASE }
enum class Distance { OWNED, CLOSE, DISTANT }

data class Application(
    override val id: String,
    override val groupId: String?,
    override val systemId: String?,
    override val type: ComponentType?,
    override val distanceFromUs: Distance?,
    val dependents: List<Dependent> = emptyList(),
    val dependencies: List<Dependency> = emptyList(),
) : Component

data class Dependent(
    override val id: String,
    override val groupId: String?,
    override val systemId: String?,
    override val type: ComponentType?,
    override val distanceFromUs: Distance?,
) : Component

data class Dependency(
    override val id: String,
    override val groupId: String?,
    override val systemId: String?,
    override val type: ComponentType?,
    override val distanceFromUs: Distance?,
) : Component
