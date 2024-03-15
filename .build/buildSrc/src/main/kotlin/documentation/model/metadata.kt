package documentation.model

import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import kotlin.text.Charsets.UTF_8

private val metadata = Metadata.load()

data class Metadata(
    val components: List<ComponentMetadata>,
    val groups: List<GroupMetadata>,
    val systems: List<SystemMetadata>,
) {
    companion object {
        fun load(): Metadata {
            val resource = ClassPathResource("/metadata.json")
            val json = resource.getContentAsString(UTF_8)
            return loadingObjectMapper.readValue(json)
        }
    }
}

data class ComponentMetadata(
    val id: String,
    val name: String,
    val groupId: String?,
    val systemId: String?,
)

data class GroupMetadata(
    val id: String,
    val name: String,
)

data class SystemMetadata(
    val id: String,
    val name: String,
)

fun componentName(id: String): String =
    metadata.components.firstOrNull { it.id == id }?.name ?: id

fun groupName(id: String): String =
    metadata.groups.firstOrNull { it.id == id }?.name ?: id

fun systemName(id: String): String =
    metadata.systems.firstOrNull { it.id == id }?.name ?: id

fun groupIdOfComponent(componentId: String): String? =
    metadata.components.firstOrNull { it.id == componentId }?.groupId

fun systemIdOfComponent(componentId: String): String? =
    metadata.components.firstOrNull { it.id == componentId }?.systemId
