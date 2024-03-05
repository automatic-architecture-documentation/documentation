package documentation

import documentation.generators.plantuml.ApplicationContextDiagramGenerator
import documentation.generators.plantuml.DiagramDirection.LEFT_TO_RIGHT
import documentation.generators.plantuml.DiagramDirection.TOP_TO_BOTTOM
import documentation.generators.plantuml.DiagramGenerator
import documentation.generators.plantuml.MultipleApplicationsDiagramGenerator
import documentation.generators.plantuml.PlantUmlDiagramGenerator.generateDiagramAndSaveAsImage
import documentation.model.Application
import documentation.model.loadApplications
import net.sourceforge.plantuml.FileFormat
import java.io.File

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
            generateHttpApplicationContextDiagram(application, targetFolder)
            generateFullApplicationContextDiagram(application, targetFolder)
        }
}

private fun generateSimpleApplicationContextDiagram(application: Application, targetFolder: File) {
    val targetSubFolder = File(targetFolder, "simple")
    val generator = ApplicationContextDiagramGenerator(
        application = application,
        includeSystemBoundaries = false,
        includeGroupBoundaries = false,
        includeHttpEndpointsNotes = false,
    )
    generateApplicationContextDiagram(application, targetSubFolder, generator)
}

private fun generateHttpApplicationContextDiagram(application: Application, targetFolder: File) {
    val targetSubFolder = File(targetFolder, "http")
    val generator = ApplicationContextDiagramGenerator(
        application = application,
        includeSystemBoundaries = false,
        includeGroupBoundaries = false,
        includeHttpEndpointsNotes = true,
    )
    generateApplicationContextDiagram(application, targetSubFolder, generator)
}

private fun generateFullApplicationContextDiagram(application: Application, targetFolder: File) {
    val targetSubFolder = File(targetFolder, "full")
    val generator = ApplicationContextDiagramGenerator(
        application = application,
        includeSystemBoundaries = true,
        includeGroupBoundaries = true,
        includeHttpEndpointsNotes = true,
    )
    generateApplicationContextDiagram(application, targetSubFolder, generator)
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
    val targetSubFolder = File(targetFolder, "left-to-right")
    val generator = MultipleApplicationsDiagramGenerator(
        applications = applications,
        direction = LEFT_TO_RIGHT
    )
    generateApplicationsOverviewDiagram(targetSubFolder, generator)
}

private fun generateTopToBottomOverviewDiagramsFromJson(targetFolder: File, applications: List<Application>) {
    val targetSubFolder = File(targetFolder, "top-to-bottom")
    val generator = MultipleApplicationsDiagramGenerator(
        applications = applications,
        direction = TOP_TO_BOTTOM
    )
    generateApplicationsOverviewDiagram(targetSubFolder, generator)
}

private fun generateApplicationsOverviewDiagram(targetFolder: File, generator: DiagramGenerator) {
    val diagramSource = generator.plantUmlSource()
    generateDiagramAndSaveAsImage(diagramSource, targetFolder, "overview", FileFormat.PNG)
    generateDiagramAndSaveAsImage(diagramSource, targetFolder, "overview", FileFormat.SVG)
}
