package documentation

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.File
import java.io.FileOutputStream

object ImageGenerator {

    fun generateDiagramImage(inputFile: File, outputFolder: File, format: FileFormat) {
        val diagramSource = inputFile.readText()
        val fileName = inputFile.nameWithoutExtension
        generateDiagramImage(diagramSource, outputFolder, fileName, format)
    }

    fun generateDiagramImage(diagramSource: String, outputFolder: File, fileName: String, format: FileFormat) {
        val outputFile = File(outputFolder, fileName + format.fileSuffix)
        val fileFormatOption = FileFormatOption(format, false)
        val reader = SourceStringReader(diagramSource)

        outputFolder.mkdirs()

        FileOutputStream(outputFile, false)
            .use { reader.outputImage(it, fileFormatOption) }
    }
}
