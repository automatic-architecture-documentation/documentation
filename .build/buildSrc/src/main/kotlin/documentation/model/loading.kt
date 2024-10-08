package documentation.model

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

internal val loadingObjectMapper = jacksonObjectMapper()
    .disable(FAIL_ON_UNKNOWN_PROPERTIES)
    .enable(READ_UNKNOWN_ENUM_VALUES_AS_NULL)

fun loadApplications(sourceFolder: File): List<ApplicationComponent> {
    check(sourceFolder.isDirectory)
    return sourceFolder.listFiles()!!
        .filter { file -> file.extension == "json" }
        .map(loadingObjectMapper::readValue)
}
