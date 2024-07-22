package documentation.generators.asciidoc

import documentation.model.Application
import documentation.model.Distance.OWNED
import documentation.model.componentName

class EventsOverviewGenerator(applications: List<Application>) {

    private val ourApplicationsWithEvents = applications
        .filter { it.distanceFromUs == OWNED }
        .filter { it.events.isNotEmpty() }
        .sortedBy { componentName(it.id) }

    fun asciiDocSource(): String =
        buildString {
            appendLine(":toc: left")
            appendLine(":toclevels: 2")
            appendLine()
            appendLine("= Events")
            appendLine()
            ourApplicationsWithEvents
                .forEach { application ->
                    appendLine("== ${componentName(application.id)}")
                    appendLine()
                    application.events
                        .sortedBy { it.name }
                        .forEach { event ->
                            appendLine("=== ${event.name}")
                            appendLine()
                            appendLine(event.description)
                            appendLine()
                            appendLine(".${event.name} Example")
                            appendLine("[source,json]")
                            appendLine("----")
                            appendLine(event.example)
                            appendLine("----")
                            appendLine()
                            appendLine(".${event.name} Field Description")
                            appendLine("[width=100%, cols=\"~,~,~,~\"]")
                            appendLine("|===")
                            appendLine("|Property |Type |Nullable? |Description")
                            appendLine()
                            event.fields
                                .forEach { field ->
                                    appendLine("|`${field.property}`")
                                    appendLine("|${field.type}")
                                    appendLine("|${field.nullable}")
                                    appendLine("|${field.description ?: ""}")
                                    appendLine()
                                }
                            appendLine("|===")
                            appendLine()
                        }
                }

        }
}
