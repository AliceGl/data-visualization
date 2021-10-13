import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer

class MainRenderer(val layer: SkiaLayer): SkiaRenderer {
    private val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    private val headerFont = Font(typeface, 40f)
    private val basicFont = Font(typeface, 10f)
    private val blackPaint = Paint().apply {
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
            MainWindowStatus.ChoosingChart -> drawChoosingChartScreen(canvas, w, h)
            MainWindowStatus.ChartParameters -> drawChartParametersScreen(canvas, w, h)
        }

        layer.needRedraw()
    }

    private fun drawChoosingChartScreen(canvas: Canvas, w: Int, h : Int) {
        val header = "Выберите тип диаграммы"
        canvas.drawString(header, (w - headerFont.measureTextWidth(header)) / 2, headerFont.size + 10f, headerFont, blackPaint)
        val headerH = headerFont.size + 20f // Высота заголовка с отступами
        val chunkW = w.toFloat() / 3
        val chunkH = (h - headerH) / 3 // Размеры одной части экрана, в которой помещается один вид диаграммы

        for (chunkX in 0..2) {
            for (chunkY in 0..2) {
                val chartIdx = chunkY * 3 + chunkX
                val chartType = ChartType.values().getOrNull(chartIdx) ?: continue
                val chartName = nameOfChart[chartType]
                require(chartName != null)
                val x = chunkX * chunkW
                val y = headerH + chunkY * chunkH

                // кнопка с названием диаграммы
                canvas.drawString(chartName,
                    x + (chunkW - basicFont.measureTextWidth(chartName)) / 2,
                    y + chunkH - 10f,
                    basicFont, blackPaint)
                val rectX0 = x + 5f
                val rectX1 = x + chunkW - 5f
                val rectY0 = y + chunkH - 25f
                val rectY1 = y + chunkH - 5f
                canvas.drawPolygon(arrayOf(
                    Point(rectX0, rectY0), Point(rectX0, rectY1),
                    Point(rectX1, rectY1), Point(rectX1, rectY0),
                    Point(rectX0, rectY0)
                ), blackPaint)

                // предпросмотр диаграммы
                chartDrawFunction[chartType]?.invoke(canvas,
                    x, y, chunkW, chunkH - 30f)
            }
        }
    }

    private fun drawChartParametersScreen(canvas: Canvas, w: Int, h: Int) {
        TODO()
    }
}

class ChartRenderer(val layer: SkiaLayer): SkiaRenderer {
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

fun drawBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    TODO()
}

fun drawStackedBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    TODO()
}

fun drawNormStackedBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    TODO()
}

fun drawLineChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    TODO()
}

fun drawAreaChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    TODO()
}

fun drawNormAreaChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    TODO()
}

fun drawPieChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    TODO()
}

fun drawRadarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    TODO()
}