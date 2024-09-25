package documentation.generators.plantuml

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import java.io.File
import java.io.FileOutputStream

object PlantUmlDiagramGenerator {

    var debug = false

    fun generateDiagramAndSaveAsImage(diagramSource: String, outputFolder: File, fileName: String, format: FileFormat) {
        val outputFile = File(outputFolder, fileName + format.fileSuffix)
        val fileFormatOption = FileFormatOption(format, false)
        val scaledDiagramSource = scale(diagramSource, format)
        val reader = SourceStringReader(scaledDiagramSource)

        outputFolder.mkdirs()

        FileOutputStream(outputFile, false)
            .use { reader.outputImage(it, fileFormatOption) }

        if (debug) {
            val debugFile = File(outputFolder, "$fileName.puml")
            FileOutputStream(debugFile, false)
                .use { fos -> fos.bufferedWriter().use { bw -> bw.write(diagramSource) } }
        }
    }

    private fun scale(diagramSource: String, format: FileFormat) = when (format) {
        FileFormat.PNG -> diagramSource.replace("scale 1", "scale 2")
        else -> diagramSource
    }
}
