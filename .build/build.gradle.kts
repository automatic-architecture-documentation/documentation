import documentation.generateComponentDiagramsFromJson
import documentation.generateDiagramsFromPlantUml
import documentation.generateOverviewDiagramsFromJson

tasks.register("generateFiles") {
    val srcFolder = File(project.rootDir, "src")
    val rootFolder = project.rootDir.parentFile
    doLast {
        generateDiagramsFromPlantUml(srcFolder, rootFolder)
        generateComponentDiagramsFromJson(srcFolder, rootFolder)
        generateOverviewDiagramsFromJson(srcFolder, rootFolder)
    }
}
