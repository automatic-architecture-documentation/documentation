package documentation.model

import documentation.model.Component.Distance
import documentation.model.Component.Type

data class Dependent(
    override val id: String,
    override val contextId: String?,
    override val systemId: String?,
    override val type: Type?,
    override val distanceFromUs: Distance?,
) : Component
