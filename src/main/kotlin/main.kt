import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
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

fun main(args: Array<String>) {
    if (args.isEmpty())
        return
    try {
        val inputData = getDataFromFile(args[0])
        createWindow("pf-2021-viz")
    } catch (e : Exception) {
        println(e.message)
    }
}

fun createWindow(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = Renderer(window.layer)
    window.layer.addMouseMotionListener(MyMouseMotionAdapter)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100,100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

class Renderer(val layer: SkiaLayer): SkiaRenderer {
    val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    val font = Font(typeface, 40f)
    val blackPaint = Paint().apply {
        color = 0xff000000.toInt()
        mode = PaintMode.FILL
        strokeWidth = 1f
    }

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()

        // РИСОВАНИЕ

        layer.needRedraw()
    }
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