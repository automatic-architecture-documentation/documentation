package documentation.tasks

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import documentation.model.Application
import documentation.model.Dependency
import documentation.model.Dependent
import documentation.model.Event
import documentation.model.HttpEndpoint
import java.io.File
import kotlin.reflect.KClass

private val objectMapper = jacksonObjectMapper()
    .setSerializationInclusion(Include.NON_EMPTY)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    .enable(SerializationFeature.INDENT_OUTPUT)

fun generateComponentDescription(sourceFolder: File, targetFolder: File, applicationId: String) {
    val baseApplicationDescription = loadBaseApplicationDescription(sourceFolder, applicationId)
    val dependents = loadDependents(sourceFolder)
    val dependencies = loadDependencies(sourceFolder)
    val events = loadEvents(sourceFolder)

    val applicationDescription = baseApplicationDescription
        .copy(dependents = dependents, dependencies = dependencies, events = events)

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
