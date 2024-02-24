package documentation.model

import documentation.model.Component.Relationship
import documentation.model.Component.Type

data class RootComponent(
    override val id: String,
    override val contextId: String?,
    override val systemId: String?,
    override val type: Type?,
    override val relationship: Relationship?,
    val dependents: List<Dependent> = emptyList(),
    val dependencies: List<Dependency> = emptyList(),
) : Component
