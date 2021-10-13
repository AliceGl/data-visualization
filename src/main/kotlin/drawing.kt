import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer

class MainRenderer(val layer: SkiaLayer): SkiaRenderer {
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

        when(status) {
            MainWindowStatus.ChoosingDiagram -> drawChoosingDiagramScreen(canvas, w, h)
            MainWindowStatus.DiagramParameters -> drawDiagramParametersScreen(canvas, w, h)
        }

        layer.needRedraw()
    }
}

fun drawChoosingDiagramScreen(canvas: Canvas, w: Int, h : Int) {
    TODO()
}

fun drawDiagramParametersScreen(canvas: Canvas, w: Int, h: Int) {
    TODO()
}

class DiagramRenderer(val layer: SkiaLayer): SkiaRenderer {
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

        TODO()

        layer.needRedraw()
    }
}