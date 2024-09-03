package documentation.model

import com.fasterxml.jackson.annotation.JsonIgnore

// (!) The content of this file is copied from repository to repository.
// It is the model used by the various generation Gradle tasks and is the same for all repositories.
// In a real project this would likely be packaged as library package and distributed using some kind of registry.

sealed interface Component {
    val id: String
    val type: ComponentType?
    val distanceFromUs: Distance?

    @get:JsonIgnore
    val groupId: String?
        get() = groupIdOfComponent(id)

    @get:JsonIgnore
    val systemId: String?
        get() = systemIdOfComponent(id)
}

data class Application(
    override val id: String,
    override val type: ComponentType?,
    override val distanceFromUs: Distance?,
    val dependents: List<Dependent> = emptyList(),
    val dependencies: List<Dependency> = emptyList(),
    val events: List<Event> = emptyList(),
    val databases: List<Database> = emptyList(),
    val messaging: Messaging = Messaging(),
) : Component

data class Dependent(
    override val id: String,
    override val type: ComponentType?,
    override val distanceFromUs: Distance?,
) : Component

data class Dependency(
    override val id: String,
    override val type: ComponentType?,
    override val distanceFromUs: Distance?,
    val credentials: List<Credentials> = emptyList(),
    val httpEndpoints: List<HttpEndpoint> = emptyList(),
) : Component

enum class ComponentType { BACKEND, FRONTEND, DATABASE }
enum class Distance { OWNED, CLOSE, DISTANT }
enum class Credentials(val label: String) { JWT("JWT"), BASIC_AUTH("Basic-Auth") }

data class HttpEndpoint(val method: String, val path: String)

data class Event(
    val name: String,
    val type: String,
    val description: String,
    val example: String,
    val fields: List<Field> = emptyList(),
) {
    data class Field(
        val property: String,
        val type: String,
        val nullable: Boolean,
        val description: String?,
    )
}

data class Messaging(
    val publishedMessages: List<PublishedMessage> = emptyList(),
    val consumedQueues: List<ConsumedQueue> = emptyList(),
) {
    data class PublishedMessage(
        val exchange: String,
        val routingKeys: List<String>,
    )

    data class ConsumedQueue(
        val name: String,
        val bindings: List<Binding>,
    )

    data class Binding(
        val exchange: String,
        val routingKeyPattern: String,
    )
}

data class Database(
    val id: String,
    val name: String,
    val type: String,
    val description: String?,
    val tables: List<Table> = emptyList(),
) {
    data class Table(
        val name: String,
        val description: String?,
        val columns: List<Column> = emptyList(),
    )

    data class Column(
        val name: String,
        val dataType: String,
        val defaultValue: String?,
        val nullable: Boolean,
        val description: String?,
        val partOfPrimaryKey: Boolean,
    )
}
