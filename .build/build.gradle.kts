import documentation.generateComponentDiagramsFromJson
import documentation.generateDiagramsFromPlantUml
import documentation.generateEndpointOverviewDocumentFromJson
import documentation.generateEventsOverviewDocumentFromJson
import documentation.generateOverviewDiagramsFromJson
import documentation.tasks.generateComponentDescription

tasks.register("generateFiles") {
    val srcFolder = File(project.rootDir, "src")
    val rootFolder = project.rootDir.parentFile
    doLast {
        generateDiagramsFromPlantUml(srcFolder, rootFolder)
        generateComponentDiagramsFromJson(srcFolder, rootFolder)
        generateOverviewDiagramsFromJson(srcFolder, rootFolder)
        generateEndpointOverviewDocumentFromJson(srcFolder, rootFolder)
        generateEventsOverviewDocumentFromJson(srcFolder, rootFolder)
    }
}

tasks.register("combineParts") {
    val sourceFolder = File(project.rootDir, "tmp/parts")
    val targetFolder = File(project.rootDir, "src/json/components")
    val componentId = project.property("componentId") as String
    doLast {
        generateComponentDescription(sourceFolder, targetFolder, componentId)
    }
}
