import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.Surface
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.io.File
import javax.swing.WindowConstants
import kotlin.system.exitProcess

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

fun checkData(inputData: InputData) {
    if (inputData.data.isEmpty())
        throw NotEnoughData()
    val len = inputData.firstRow.size
    if (len == 0)
        throw NotEnoughData()
    inputData.data.forEach {
        if (it.size != len)
            throw WrongDataFormat()
        if (!it.all {x -> x >= 0})
            throw WrongDataFormat()
    }
}

enum class MainWindowStatus { ChoosingChart, ChartParameters }

var status = MainWindowStatus.ChoosingChart

data class Button(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val clickAction: () -> (Unit))
val buttons : MutableList<Button> = mutableListOf()

lateinit var inputData : InputData

val surface = Surface.makeRasterN32Premul(800, 600)

fun main(args: Array<String>) {
    if (args.isEmpty())
        return
    try {
        inputData = getDataFromFile(args[0])
        checkData(inputData)
        createChartWindow("chart")
        createMainWindow("menu")
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

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100,100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

fun createChartWindow(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.setLocation(100, 100)
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = ChartRenderer(window.layer)
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
    override fun mouseClicked(e: MouseEvent) {
        buttons.forEach {
            if (it.x0 < e.x && e.x < it.x1 && it.y0 < e.y && e.y < it.y1)
                it.clickAction.invoke()
        }
    }
}

fun saveInFileAndExit() {
    val pngBytes = surface.makeImageSnapshot().encodeToData()!!.bytes
    File("output.png").writeBytes(pngBytes)
    exitProcess(0)
}