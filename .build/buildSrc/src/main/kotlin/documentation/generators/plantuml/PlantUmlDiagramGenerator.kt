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
        val reader = SourceStringReader(diagramSource)

        outputFolder.mkdirs()

        FileOutputStream(outputFile, false)
            .use { reader.outputImage(it, fileFormatOption) }

        if (debug) {
            val debugFile = File(outputFolder, "$fileName.puml")
            FileOutputStream(debugFile, false)
                .use { fos -> fos.bufferedWriter().use { bw -> bw.write(diagramSource) } }
        }
    }
}
