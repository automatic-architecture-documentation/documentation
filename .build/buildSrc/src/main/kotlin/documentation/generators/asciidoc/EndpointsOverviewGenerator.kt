package documentation.generators.asciidoc

import documentation.generators.componentName
import documentation.model.Application
import documentation.model.ComponentType.BACKEND
import documentation.model.Dependency
import documentation.model.Distance.CLOSE
import documentation.model.Distance.DISTANT
import documentation.model.Distance.OWNED
import documentation.model.HttpEndpoint

class EndpointsOverviewGenerator(applications: List<Application>) {

    private val relevantComponentTypes = setOf(BACKEND)

    private val ourApplicationsAndTheirDependencies = applications
        .filter { it.distanceFromUs == OWNED }
        .map { application -> application to application.dependencies }

    fun asciiDocSource(): String =
        buildString {

            val allCalledComponents = ourApplicationsAndTheirDependencies
                .flatMap { (_, dependencies) -> dependencies }
                .filter { dependency -> dependency.type in relevantComponentTypes }
                .filter { dependency -> dependency.httpEndpoints.isNotEmpty() }
                .sortedBy { dependency -> componentName(dependency.id) }

            appendLine("= Used Endpoints")
            appendLine()
            appendLine("NOTE: This overview includes all endpoints _known_ to be called.")
            appendLine()

            val ourCalledComponents = allCalledComponents.filter { it.distanceFromUs == OWNED }
            if (ourCalledComponents.isNotEmpty()) {
                appendLine()
                appendLine("== Our Components")
                appendLine()
                appendComponentsAndTheirEndpoints(ourCalledComponents)
                appendLine()
            }

            val closeComponents = allCalledComponents.filter { it.distanceFromUs == CLOSE }
            if (closeComponents.isNotEmpty()) {
                appendLine()
                appendLine("== Components Close to Us")
                appendLine()
                appendComponentsAndTheirEndpoints(closeComponents)
                appendLine()
            }

            val distantComponents = allCalledComponents.filter { it.distanceFromUs == DISTANT }
            if (distantComponents.isNotEmpty()) {
                appendLine()
                appendLine("== Components Distant from Us")
                appendLine()
                appendComponentsAndTheirEndpoints(distantComponents)
                appendLine()
            }
        }

    private fun StringBuilder.appendComponentsAndTheirEndpoints(dependencies: List<Dependency>) {
        groupEndpointsByDependencyId(dependencies)
            .sortedBy { componentName(it.id) }
            .forEach { (id, endpoints) ->
                appendLine()
                appendLine("**${componentName(id)}**")
                appendLine()
                endpoints
                    .sortedWith(compareBy(HttpEndpoint::path, HttpEndpoint::method))
                    .forEach { endpoint ->
                        val applicationIds = findOurCallingApplicationIds(id, endpoint)
                        val endpointLine = buildString {
                            append("* `")
                            append(endpoint.method)
                            append(" ")
                            append(endpoint.path)
                            append("`")
                            if (applicationIds.isNotEmpty()) {
                                val applicationNames = applicationIds.joinToString(
                                    prefix = "_",
                                    separator = "_, _",
                                    postfix = "_",
                                    transform = ::componentName
                                )
                                append(" used by ")
                                append(applicationNames)
                            }
                        }
                        appendLine(endpointLine)
                    }
            }
    }

    private fun findOurCallingApplicationIds(dependencyId: String, endpoint: HttpEndpoint): List<String> =
        ourApplicationsAndTheirDependencies
            .filter { (_, dependencies) -> dependencies.any { it.id == dependencyId } }
            .filter { (_, dependencies) -> dependencies.flatMap { it.httpEndpoints }.contains(endpoint) }
            .map { (application, _) -> application.id }

    private fun groupEndpointsByDependencyId(dependencies: List<Dependency>): List<DependencyIdAndEndpoints> =
        dependencies
            .groupBy(Dependency::id, Dependency::httpEndpoints)
            .mapValues { (_, endpoints) -> endpoints.flatten().toSet() }
            .map { DependencyIdAndEndpoints(it.key, it.value) }

    private data class DependencyIdAndEndpoints(val id: String, val endpoints: Set<HttpEndpoint>)
}
