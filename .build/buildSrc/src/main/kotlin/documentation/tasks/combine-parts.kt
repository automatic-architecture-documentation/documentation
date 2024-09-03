package documentation.tasks

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import documentation.model.Application
import documentation.model.Database
import documentation.model.Dependency
import documentation.model.Dependent
import documentation.model.Event
import documentation.model.HttpEndpoint
import documentation.model.Messaging
import documentation.model.Messaging.ConsumedQueue
import documentation.model.Messaging.PublishedMessage
import java.io.File
import kotlin.reflect.KClass

private val objectMapper = jacksonObjectMapper()
    .setSerializationInclusion(Include.NON_EMPTY)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    .enable(SerializationFeature.INDENT_OUTPUT)

fun generateComponentDescription(sourceFolder: File, targetFolder: File, applicationId: String) {
    val applicationDescription = loadBaseApplicationDescription(sourceFolder, applicationId)
        .copy(
            dependents = loadDependents(sourceFolder),
            dependencies = loadDependencies(sourceFolder),
            events = loadEvents(sourceFolder),
            databases = loadDatabases(sourceFolder),
            messaging = Messaging(
                publishedMessages = loadPublishedMessages(sourceFolder),
                consumedQueues = loadConsumedQueues(sourceFolder),
            )
        )

    val file = File(targetFolder, applicationDescription.id + ".json")
    objectMapper.writeValue(file, applicationDescription)
}

private fun loadBaseApplicationDescription(sourceFolder: File, applicationId: String): Application {
    val file = File(sourceFolder, "$applicationId.json")
    check(file.isFile) { "File not found: $file" }
    return objectMapper.readValue<Application>(file)
}

private fun loadDependents(sourceFolder: File): List<Dependent> =
    listJsonFilesInFolder(File(sourceFolder, "dependents"))
        .map { file -> loadDependent(file) }

private fun loadDependent(file: File): Dependent {
    return objectMapper.readValue<Dependent>(file)
}

private fun loadDependencies(sourceFolder: File): List<Dependency> =
    listJsonFilesInFolder(File(sourceFolder, "dependencies"))
        .map { file -> loadDependency(file) }

private fun loadDependency(file: File): Dependency {
    val dependency = objectMapper.readValue<Dependency>(file)

    var httpEndpoints = emptyList<HttpEndpoint>()

    val httpEndpointsFile = File(file.parentFile, "http-endpoints/${dependency.id}.jsonl")
    if (httpEndpointsFile.isFile) {
        httpEndpoints = loadFromJsonListFile(httpEndpointsFile, HttpEndpoint::class)
            .sortedWith(compareBy(HttpEndpoint::path, HttpEndpoint::method))
            .distinct()
    }

    return dependency.copy(httpEndpoints = httpEndpoints)
}

private fun loadEvents(sourceFolder: File): List<Event> =
    listJsonFilesInFolder(File(sourceFolder, "events"))
        .map { file -> loadEvent(file) }

private fun loadEvent(file: File): Event {
    return objectMapper.readValue<Event>(file)
}

fun loadPublishedMessages(sourceFolder: File): List<PublishedMessage> {
    val messagingFolder = File(sourceFolder, "messaging")
    val file = File(messagingFolder, "published-messages.jsonl")
    if (file.isFile) {
        return loadFromJsonListFile(file, PublishedMessage::class)
            .groupBy(PublishedMessage::exchange, PublishedMessage::routingKeys)
            .mapValues { (_, routingKeys) -> routingKeys.flatten().distinct() }
            .map { (exchange, routingKeys) -> PublishedMessage(exchange, routingKeys) }
    }
    return emptyList()
}

fun loadConsumedQueues(sourceFolder: File): List<ConsumedQueue> {
    val messagingFolder = File(sourceFolder, "messaging")
    val file = File(messagingFolder, "consumed-queues.jsonl")
    if (file.isFile) {
        return loadFromJsonListFile(file, ConsumedQueue::class)
            .groupBy(ConsumedQueue::name, ConsumedQueue::bindings)
            .mapValues { (_, bindings) -> bindings.flatten().distinct() }
            .map { (name, bindings) -> ConsumedQueue(name, bindings) }
    }
    return emptyList()
}

fun loadDatabases(sourceFolder: File): List<Database> =
    listJsonFilesInFolder(File(sourceFolder, "databases"))
        .map { file -> loadDatabase(file) }

private fun loadDatabase(file: File): Database {
    return objectMapper.readValue<Database>(file)
}

private fun listJsonFilesInFolder(folder: File): List<File> =
    if (folder.isDirectory) {
        folder.listFiles()!!
            .filter { it.isFile }
            .filter { it.extension == "json" }
    } else {
        emptyList()
    }

private fun <T : Any> loadFromJsonListFile(file: File, clazz: KClass<T>): List<T> =
    file.readLines()
        .filter(String::isNotBlank)
        .map(String::trim)
        .map { objectMapper.readValue(it, clazz.java) }
