import documentation.DetailedDiagramGenerator
import documentation.ImageGenerator.generateDiagramImage
import documentation.OverviewDiagramGenerator
import documentation.model.RootComponent
import documentation.model.loadComponents
import net.sourceforge.plantuml.FileFormat

tasks.register("generate-images", GenerateImagesTask::class)

open class GenerateImagesTask : DefaultTask() {

    @TaskAction
    fun execute() {
        generateComponentImagesFromPumlFiles()
        generateComponentImagesFromJsonFiles()
        generateOverviewImages()
    }

    private fun generateComponentImagesFromPumlFiles() {
        val sourceFolder = File(project.rootDir, "src/plantuml/components")
        val targetFolder = File(project.rootDir, "documentation/components/puml")
        check(sourceFolder.isDirectory)
        sourceFolder.listFiles()!!
            .filter { file -> file.extension == "puml" }
            .forEach { inputFile ->
                generateDiagramImage(inputFile, targetFolder, FileFormat.PNG)
                generateDiagramImage(inputFile, targetFolder, FileFormat.SVG)
            }
    }

    private fun generateComponentImagesFromJsonFiles() {
        val sourceFolder = File(project.rootDir, "src/json/components")
        val targetFolder = File(project.rootDir, "documentation/components/json")

        loadComponents(sourceFolder)
            .forEach { generateDetailedDiagram(it, targetFolder) }
    }

    private fun generateDetailedDiagram(component: RootComponent, targetFolder: File) {
        val diagramSource = DetailedDiagramGenerator(component).generate()

        generateDiagramImage(diagramSource, targetFolder, component.id, FileFormat.PNG)
        generateDiagramImage(diagramSource, targetFolder, component.id, FileFormat.SVG)
    }

    private fun generateOverviewImages() {
        val sourceFolder = File(project.rootDir, "src/json/components")
        val targetFolder = File(project.rootDir, "documentation/overview")

        val components = loadComponents(sourceFolder)
        val diagramSource = OverviewDiagramGenerator(components).generate()

        generateDiagramImage(diagramSource, targetFolder, "overview", FileFormat.PNG)
        generateDiagramImage(diagramSource, targetFolder, "overview", FileFormat.SVG)
    }
}
