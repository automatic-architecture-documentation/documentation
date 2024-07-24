package documentation

import documentation.generators.asciidoc.EndpointsOverviewGenerator
import documentation.generators.asciidoc.EventsOverviewGenerator
import documentation.generators.plantuml.ApplicationContextDiagramGenerator
import documentation.generators.plantuml.DiagramDirection.LEFT_TO_RIGHT
import documentation.generators.plantuml.DiagramDirection.TOP_TO_BOTTOM
import documentation.generators.plantuml.DiagramGenerator
import documentation.generators.plantuml.LineType
import documentation.generators.plantuml.LineType.DEFAULT
import documentation.generators.plantuml.LineType.ORTHOGONAL
import documentation.generators.plantuml.LineType.POLY
import documentation.generators.plantuml.MultipleApplicationsDiagramGenerator
import documentation.generators.plantuml.PlantUmlDiagramGenerator.generateDiagramAndSaveAsImage
import documentation.model.Application
import documentation.model.ComponentType.BACKEND
import documentation.model.ComponentType.FRONTEND
import documentation.model.loadApplications
import net.sourceforge.plantuml.FileFormat
import java.io.File
import java.io.FileOutputStream

private val lineTypes: List<LineType> = listOf(DEFAULT, POLY, ORTHOGONAL)

// PLANTUML DIAGRAMS

fun generateDiagramsFromPlantUml(srcFolder: File, rootFolder: File) {
    val sourcesFolder = File(srcFolder, "plantuml")
    val targetFolder = File(rootFolder, "diagrams")

    val sourceFolderPath = sourcesFolder.toPath()
    sourcesFolder.walkTopDown()
        .filter { it.extension == "puml" }
        .forEach { file ->
            val folderPath = file.parentFile.toPath()
            val relativePath = sourceFolderPath.relativize(folderPath).toString()

            val diagramSource = file.readText()
            val targetSubFolder = when (relativePath) {
                "" -> targetFolder
                else -> File(targetFolder, relativePath)
            }
            val name = file.nameWithoutExtension

            generateDiagramAndSaveAsImage(diagramSource, targetSubFolder, name, FileFormat.PNG)
            generateDiagramAndSaveAsImage(diagramSource, targetSubFolder, name, FileFormat.SVG)
        }
}

// APPLICATION CONTEXT DIAGRAMS

fun generateComponentDiagramsFromJson(srcFolder: File, rootFolder: File) {
    val sourcesFolder = File(srcFolder, "json/components")
    val targetFolder = File(rootFolder, "diagrams/components")

    loadApplications(sourcesFolder)
        .forEach { application ->
            generateSimpleApplicationContextDiagram(application, targetFolder)
            generateBoundaryApplicationContextDiagram(application, targetFolder)
            generateHttpApplicationContextDiagram(application, targetFolder)
            generateFullApplicationContextDiagram(application, targetFolder)
        }
}

private fun generateSimpleApplicationContextDiagram(application: Application, targetFolder: File) {
    lineTypes { lineType, name ->
        val targetSubFolder = File(targetFolder, "simple_$name")
        val generator = ApplicationContextDiagramGenerator(
            application = application,
            options = ApplicationContextDiagramGenerator.Options(
                lineType = lineType,
            ),
        )
        generateApplicationContextDiagram(application, targetSubFolder, generator)
    }
}

private fun generateBoundaryApplicationContextDiagram(application: Application, targetFolder: File) {
    lineTypes { lineType, name ->
        val targetSubFolder = File(targetFolder, "boundary_$name")
        val generator = ApplicationContextDiagramGenerator(
            application = application,
            options = ApplicationContextDiagramGenerator.Options(
                includeSystemBoundaries = true,
                includeGroupBoundaries = true,
                lineType = lineType,
            )
        )
        generateApplicationContextDiagram(application, targetSubFolder, generator)
    }
}

private fun generateHttpApplicationContextDiagram(application: Application, targetFolder: File) {
    lineTypes { lineType, name ->
        val targetSubFolder = File(targetFolder, "http_$name")
        val generator = ApplicationContextDiagramGenerator(
            application = application,
            options = ApplicationContextDiagramGenerator.Options(
                includedComponentTypes = setOf(BACKEND, FRONTEND),
                includeCredentials = true,
                includeHttpEndpointsNotes = true,
                lineType = lineType,
            )
        )
        generateApplicationContextDiagram(application, targetSubFolder, generator)
    }
}

private fun generateFullApplicationContextDiagram(application: Application, targetFolder: File) {
    lineTypes { lineType, name ->
        val targetSubFolder = File(targetFolder, "full_$name")
        val generator = ApplicationContextDiagramGenerator(
            application = application,
            options = ApplicationContextDiagramGenerator.Options(
                includeSystemBoundaries = true,
                includeGroupBoundaries = true,
                includeCredentials = true,
                includeHttpEndpointsNotes = true,
                lineType = lineType,
            )
        )
        generateApplicationContextDiagram(application, targetSubFolder, generator)
    }
}

private fun generateApplicationContextDiagram(
    application: Application,
    targetFolder: File,
    generator: DiagramGenerator
) {
    val diagramSource = generator.plantUmlSource()
    generateDiagramAndSaveAsImage(diagramSource, targetFolder, application.id, FileFormat.PNG)
    generateDiagramAndSaveAsImage(diagramSource, targetFolder, application.id, FileFormat.SVG)
}

// APPLICATIONS OVERVIEW DIAGRAMS

fun generateOverviewDiagramsFromJson(srcFolder: File, rootFolder: File) {
    val sourcesFolder = File(srcFolder, "json/components")
    val targetFolder = File(rootFolder, "diagrams/overview")

    val applications = loadApplications(sourcesFolder)

    generateLeftToRightOverviewDiagramsFromJson(targetFolder, applications)
    generateTopToBottomOverviewDiagramsFromJson(targetFolder, applications)
}

private fun generateLeftToRightOverviewDiagramsFromJson(targetFolder: File, applications: List<Application>) {
    lineTypes { lineType, name ->
        val targetSubFolder = File(targetFolder, "left-to-right_$name")
        val generator = MultipleApplicationsDiagramGenerator(
            applications = applications,
            options = MultipleApplicationsDiagramGenerator.Options(
                direction = LEFT_TO_RIGHT,
                lineType = lineType,
                includeCredentials = true,
            ),
        )
        generateApplicationsOverviewDiagram(targetSubFolder, generator)
    }
}

private fun generateTopToBottomOverviewDiagramsFromJson(targetFolder: File, applications: List<Application>) {
    lineTypes { lineType, name ->
        val targetSubFolder = File(targetFolder, "top-to-bottom_$name")
        val generator = MultipleApplicationsDiagramGenerator(
            applications = applications,
            options = MultipleApplicationsDiagramGenerator.Options(
                direction = TOP_TO_BOTTOM,
                lineType = lineType,
                includeCredentials = true,
            ),
        )
        generateApplicationsOverviewDiagram(targetSubFolder, generator)
    }
}

private fun generateApplicationsOverviewDiagram(targetFolder: File, generator: DiagramGenerator) {
    val diagramSource = generator.plantUmlSource()
    generateDiagramAndSaveAsImage(diagramSource, targetFolder, "overview", FileFormat.PNG)
    generateDiagramAndSaveAsImage(diagramSource, targetFolder, "overview", FileFormat.SVG)
}

// USED ENDPOINTS

fun generateEndpointOverviewDocumentFromJson(srcFolder: File, rootFolder: File) {
    val sourcesFolder = File(srcFolder, "json/components")
    val targetFolder = File(rootFolder, "documents")
    val targetFile = File(targetFolder, "endpoints.adoc")

    val applications = loadApplications(sourcesFolder)

    val generator = EndpointsOverviewGenerator(applications)
    val source = generator.asciiDocSource()

    createOrReplaceFile(targetFile, source)
}

// EVENTS

fun generateEventsOverviewDocumentFromJson(srcFolder: File, rootFolder: File) {
    val sourcesFolder = File(srcFolder, "json/components")
    val targetFolder = File(rootFolder, "documents")
    val targetFile = File(targetFolder, "events.adoc")

    val applications = loadApplications(sourcesFolder)

    val generator = EventsOverviewGenerator(applications)
    val source = generator.asciiDocSource()

    createOrReplaceFile(targetFile, source)
}

// COMMON

fun lineTypes(block: (LineType, String) -> Unit) {
    lineTypes.forEach { block(it, it.name.lowercase()) }
}

private fun createOrReplaceFile(file: File, source: String) {
    file.parentFile.mkdirs()
    FileOutputStream(file, false).use { fos ->
        fos.bufferedWriter().use { bw ->
            bw.write(source)
        }
    }
}
