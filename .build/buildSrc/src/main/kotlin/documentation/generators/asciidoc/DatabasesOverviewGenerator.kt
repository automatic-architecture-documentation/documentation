package documentation.generators.asciidoc

import documentation.model.Application
import documentation.model.Distance.OWNED
import documentation.model.componentName

class DatabasesOverviewGenerator(applications: List<Application>) {

    private val ourApplicationsWithDatabases = applications
        .filter { it.distanceFromUs == OWNED }
        .filter { it.databases.isNotEmpty() }
        .sortedBy { componentName(it.id) }

    fun asciiDocSource(): String =
        buildString {
            appendLine(":toc: left")
            appendLine(":toclevels: 2")
            appendLine()
            appendLine("= Databases")
            appendLine()
            ourApplicationsWithDatabases
                .forEach { application ->
                }
        }
}
