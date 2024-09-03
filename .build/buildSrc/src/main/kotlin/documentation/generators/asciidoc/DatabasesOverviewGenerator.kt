package documentation.generators.asciidoc

import documentation.model.Application
import documentation.model.Database
import documentation.model.Database.Table
import documentation.model.Distance.OWNED

class DatabasesOverviewGenerator(applications: List<Application>) {

    private val keyEmoji = "\uD83D\uDD11"
    private val ourDatabases = applications
        .filter { it.distanceFromUs == OWNED }
        .flatMap(Application::databases)
        .sortedBy(Database::name)

    fun asciiDocSource(): String =
        buildString {
            appendLine(":toc: left")
            appendLine(":toclevels: 3")
            appendLine()
            appendLine("= Databases")
            appendLine()
            ourDatabases
                .sortedBy(Database::name)
                .forEach { database ->
                    appendLine("== ${database.name}")
                    appendLine()
                    if (database.description != null) {
                        appendLine(database.description)
                        appendLine()
                    }
                    appendLine("Type: ${database.type}")
                    appendLine()
                    database.tables
                        .sortedBy(Table::name)
                        .forEach { table ->
                            appendLine("=== Table: ${table.name}")
                            appendLine()
                            if (table.description != null) {
                                appendLine(table.description)
                                appendLine()
                            }
                            appendLine(".${table.name} columns")
                            appendLine("[width=100%, cols=\"~,~,~,~,~\"]")
                            appendLine("|===")
                            appendLine("|Column |Type |Nullable? |Default Value |Description")
                            appendLine()
                            table.columns
                                .forEach { column ->
                                    if (column.partOfPrimaryKey) {
                                        appendLine("|`$keyEmoji ${column.name}`")
                                    } else {
                                        appendLine("|`${column.name}`")
                                    }
                                    appendLine("|${column.dataType}")
                                    appendLine("|${column.nullable}")
                                    appendLine("|${column.defaultValue ?: ""}")
                                    appendLine("|${column.description ?: ""}")
                                    appendLine()
                                }
                            appendLine("|===")
                            appendLine()
                        }
                }
        }
}
