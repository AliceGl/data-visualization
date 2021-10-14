import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer

class MainRenderer(private val layer: SkiaLayer): SkiaRenderer {

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

        buttons.clear()
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
                buttons.add(Button(rectX0, rectY0, rectX1, rectY1) {
                    status = MainWindowStatus.ChartParameters
                    chosenChart = chartType
                })

                // предпросмотр диаграммы
                chartDrawFunction[chartType]?.invoke(canvas,
                    x, y, chunkW, chunkH - 30f)
            }
        }
    }

    private fun drawChartParametersScreen(canvas: Canvas, w: Int, h: Int) {
        require(chosenChart != null)
        val chartName = nameOfChart[chosenChart]
        require(chartName != null)
        // временно
        canvas.drawString(chartName, 30f, 30f, headerFont, blackPaint)
    }
}

class ChartRenderer(private val layer: SkiaLayer): SkiaRenderer {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()



        layer.needRedraw()
    }
}

fun drawBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawStackedBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawNormStackedBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawLineChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawAreaChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawNormAreaChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawPieChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawRadarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}