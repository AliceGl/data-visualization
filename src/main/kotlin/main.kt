import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.io.File
import javax.swing.WindowConstants

data class InputData (val names: List<String>, val firstRow: List<String>, val data: List<List<Int>>)

fun getDataFromFile(filePath: String) : InputData {
    if (!File(filePath).exists() || !File(filePath).isFile)
        throw FileNotFound(filePath)
    val lines = File(filePath).readLines()
    return InputData( lines.map {it.split(" ").getOrNull(0) ?: throw NotEnoughData()},
        lines.getOrNull(0)?.split(" ")?.drop(1) ?: throw NotEnoughData(),
        lines.drop(1).map { line ->
            line.split(" ").drop(1).map {
            it.toIntOrNull() ?: throw WrongDataFormat()
        }}
    )
}

enum class MainWindowStatus { ChoosingDiagram, DiagramParameters }

val status = MainWindowStatus.ChoosingDiagram

fun main(args: Array<String>) {
    if (args.isEmpty())
        return
    try {
        val inputData = getDataFromFile(args[0])
        createMainWindow("pf-2021-viz")
    } catch (e : Exception) {
        println(e.message)
    }
}

fun createMainWindow(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = MainRenderer(window.layer)
    window.layer.addMouseMotionListener(MyMouseMotionAdapter)
    window.layer.addMouseListener(MyMouseAdapter)
    window.layer

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100,100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

fun createDiagramWindow(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = DiagramRenderer(window.layer)
    window.layer.addMouseMotionListener(MyMouseMotionAdapter)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100,100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

object State {
    var mouseX = 0f
    var mouseY = 0f
}

object MyMouseMotionAdapter : MouseMotionAdapter() {
    override fun mouseMoved(event: MouseEvent) {
        State.mouseX = event.x.toFloat()
        State.mouseY = event.y.toFloat()
    }
}

object MyMouseAdapter : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent?) {
        TODO()
    }
}