import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.FileOutputStream

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("net.sourceforge.plantuml:plantuml:1.2024.3")
    }
}

tasks.register("generate-images", GenerateImagesTask::class)

open class GenerateImagesTask : DefaultTask() {

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
            .filter { file -> file.name.endsWith(".puml") }
            .forEach { inputFile ->
                generateImage(inputFile, targetFolder, FileFormat.PNG)
                generateImage(inputFile, targetFolder, FileFormat.SVG)
            }
    }

    private fun generateImage(inputFile: File, outputFolder: File, format: FileFormat) {
        val outputFile = File(outputFolder, inputFile.nameWithoutExtension + format.fileSuffix)

        logger.info("Generating diagram form: $inputFile")
        val fileFormatOption = FileFormatOption(format, false)
        SourceStringReader(inputFile.readText()).outputImage(FileOutputStream(outputFile, false), fileFormatOption)
        logger.info("Diagram successfully created: $outputFile")
    }
}
