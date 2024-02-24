import documentation.DetailedDiagramGenerator
import documentation.ImageGenerator.generateDiagramImage
import documentation.OverviewDiagramGenerator
import documentation.model.RootComponent
import documentation.model.loadComponents
import net.sourceforge.plantuml.FileFormat

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
        classpath("com.fasterxml.jackson.core:jackson-core:2.16.1")
        classpath("com.fasterxml.jackson.core:jackson-databind:2.16.1")
        classpath("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
        classpath("net.sourceforge.plantuml:plantuml:1.2024.3")
    }
}

tasks.register("generate-overview-image", GenerateOverviewImageTask::class)
tasks.register("generate-detailed-images", GenerateDetailedImagesTask::class)
tasks.register("generate-detailed2-images", GenerateDetailed2ImagesTask::class)

open class GenerateDetailedImagesTask : DefaultTask() {

    @TaskAction
    fun execute() {
        generateImages(
            sourceFolder = File(project.rootDir, "src/plantuml/detailed"),
            targetFolder = File(project.rootDir, "documentation/detailed")
        )
    }

    private fun generateImages(sourceFolder: File, targetFolder: File) {
        check(sourceFolder.isDirectory)
        sourceFolder.listFiles()!!
            .filter { file -> file.extension == "puml" }
            .forEach { inputFile ->
                generateDiagramImage(inputFile, targetFolder, FileFormat.PNG)
                generateDiagramImage(inputFile, targetFolder, FileFormat.SVG)
            }
    }
}

open class GenerateOverviewImageTask : DefaultTask() {

    @TaskAction
    fun execute() {
        val sourceFolder = File(project.rootDir, "src/descriptions")
        val targetFolder = File(project.rootDir, "documentation/overview")

        loadComponents(sourceFolder)
            .also { generateOverviewDiagram(it, targetFolder) }
    }

    private fun generateOverviewDiagram(components: List<RootComponent>, targetFolder: File) {
        val diagramSource = OverviewDiagramGenerator(components).generate()

        generateDiagramImage(diagramSource, targetFolder, "overview", FileFormat.PNG)
        generateDiagramImage(diagramSource, targetFolder, "overview", FileFormat.SVG)
    }
}

open class GenerateDetailed2ImagesTask : DefaultTask() {

    @TaskAction
    fun execute() {
        val sourceFolder = File(project.rootDir, "src/descriptions")
        val targetFolder = File(project.rootDir, "documentation/detailed2")

        loadComponents(sourceFolder)
            .forEach { generateDetailedDiagram(it, targetFolder) }
    }

    private fun generateDetailedDiagram(component: RootComponent, targetFolder: File) {
        val diagramSource = DetailedDiagramGenerator(component).generate()

        generateDiagramImage(diagramSource, targetFolder, component.id, FileFormat.PNG)
        generateDiagramImage(diagramSource, targetFolder, component.id, FileFormat.SVG)
    }
}
